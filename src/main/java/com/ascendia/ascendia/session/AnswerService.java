package com.ascendia.ascendia.session;

import com.ascendia.ascendia.question.OptionEntity;
import com.ascendia.ascendia.question.OptionRepository;
import com.ascendia.ascendia.question.QuestionEntity;
import com.ascendia.ascendia.question.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final TestSessionRepository testSessionRepository;
    private final TestSessionQuestionRepository testSessionQuestionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Transactional
    public AnswerResponse submitAnswer(AnswerRequest request) {

        TestSessionEntity session = testSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getFinishedAt() != null)
            throw new IllegalStateException("Session already finished");

        validateQuestionBelongsToSession(request.getSessionId(), request.getQuestionId());

        QuestionEntity question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        OptionEntity selectedOption = optionRepository.findById(request.getSelectedOptionId())
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));

        if (!selectedOption.getQuestion().getId().equals(question.getId()))
            throw new IllegalArgumentException("Option does not belong to question");

        upsertAnswer(session, question, selectedOption);

        if (session.getMode() == TestMode.ASSESSMENT) {
            return buildAssessmentResponse();
        } else {
            return buildPracticeResponse(question, selectedOption);
        }
    }

    private void upsertAnswer(TestSessionEntity session,
                              QuestionEntity question,
                              OptionEntity selectedOption) {

        UserAnswerEntity answer = userAnswerRepository
                .findBySessionIdAndQuestionId(session.getId(), question.getId())
                .orElse(new UserAnswerEntity());

        answer.setSession(session);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setCorrect(selectedOption.isCorrect());
        answer.setAnsweredAt(OffsetDateTime.now());

        userAnswerRepository.save(answer);
    }

    private AnswerResponse buildAssessmentResponse() {
        AnswerResponse response = new AnswerResponse();
        response.setMode(TestMode.ASSESSMENT.name());
        response.setSaved(true);
        return response;
    }

    private AnswerResponse buildPracticeResponse(QuestionEntity question, OptionEntity selectedOption) {

        Long correctOptionId = question.getOptions().stream()
                .filter(OptionEntity::isCorrect)
                .map(OptionEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No correct option found for question"));

        AnswerResponse response = new AnswerResponse();
        response.setMode(TestMode.PRACTICE.name());
        response.setCorrect(selectedOption.isCorrect());
        response.setCorrectOptionId(correctOptionId);
        response.setExplanation(question.getExplanation());
        response.setSaved(true);
        response.setTip(question.getTip());
        return response;
    }

    private void validateQuestionBelongsToSession(UUID sessionId, Long questionId) {
        boolean belongs = testSessionQuestionRepository
                .existsBySessionIdAndQuestionId(sessionId, questionId);
        if (!belongs)
            throw new IllegalArgumentException("Question does not belong to session");
    }

    public TipResponse getTip(UUID sessionId, Long questionId) {

        TestSessionEntity session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getFinishedAt() != null)
            throw new IllegalStateException("Session already finished");

        if (session.getMode() != TestMode.PRACTICE)
            throw new IllegalStateException("Tips are only available in PRACTICE mode");

        validateQuestionBelongsToSession(sessionId, questionId);

        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        TipResponse response = new TipResponse();
        response.setQuestionId(questionId);
        response.setTip(question.getTip());
        return response;
    }
}
