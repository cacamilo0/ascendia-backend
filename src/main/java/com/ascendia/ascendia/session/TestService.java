package com.ascendia.ascendia.session;

import com.ascendia.ascendia.common.Area;
import com.ascendia.ascendia.question.*;
import com.ascendia.ascendia.user.UserEntity;
import com.ascendia.ascendia.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private static final int QUESTIONS_PER_BUCKET = 3;
    private static final int RECENT_SESSIONS_LOOKBACK = 3;

    private final TestSessionQuestionRepository testSessionQuestionRepository;
    private final TestSessionRepository testSessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final PassageRepository passageRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;

    @Transactional
    public TestResultResponse submit(TestSubmitRequest request) {

        TestSessionEntity session = resolveSession(request.getSessionId());
        validateAnswers(request.getAnswers());
        validateQuestionsBelongToSession(request.getSessionId(), request.getAnswers());

        Map<Long, QuestionEntity> questionMap = fetchQuestions(request.getAnswers());
        Map<Long, OptionEntity> optionMap = fetchOptions(request.getAnswers());

        EvaluationResult evaluation = evaluate(request.getAnswers(), questionMap, optionMap);
        evaluation.getUserAnswers().forEach(a -> a.setSession(session));

        userAnswerRepository.saveAll(evaluation.getUserAnswers());

        session.setFinishedAt(OffsetDateTime.now());
        testSessionRepository.save(session);

        return buildResponse(evaluation);
    }

    private TestSessionEntity resolveSession(UUID sessionId) {
        TestSessionEntity session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getFinishedAt() != null)
            throw new IllegalStateException("Session already finished");

        return session;
    }

    private void validateQuestionsBelongToSession(UUID sessionId, List<AnswerRequest> answers) {
        Set<Long> allowedQuestionIds = new HashSet<>(
                testSessionQuestionRepository.findQuestionIdsBySessionId(sessionId)
        );

        Set<Long> submittedQuestionIds = answers.stream()
                .map(AnswerRequest::getQuestionId)
                .collect(Collectors.toSet());

        if (!submittedQuestionIds.equals(allowedQuestionIds)) {
            throw new IllegalArgumentException("Submitted questions do not match session questions");
        }
    }

    private void validateAnswers(List<AnswerRequest> answers) {
        Set<Long> seen = new HashSet<>();
        for (AnswerRequest answer : answers) {
            if (!seen.add(answer.getQuestionId()))
                throw new IllegalArgumentException("Duplicate question in request");
        }
    }

    private Map<Long, QuestionEntity> fetchQuestions(List<AnswerRequest> answers) {
        Set<Long> ids = answers.stream()
                .map(AnswerRequest::getQuestionId)
                .collect(Collectors.toSet());

        List<QuestionEntity> questions = questionRepository.findAllById(ids);

        if (questions.size() != ids.size())
            throw new IllegalArgumentException("Some questions not found");

        return questions.stream().collect(Collectors.toMap(QuestionEntity::getId, q -> q));
    }

    private Map<Long, OptionEntity> fetchOptions(List<AnswerRequest> answers) {
        Set<Long> ids = answers.stream()
                .map(AnswerRequest::getSelectedOptionId)
                .collect(Collectors.toSet());

        List<OptionEntity> options = optionRepository.findAllById(ids);

        if (options.size() != ids.size())
            throw new IllegalArgumentException("Some options not found");

        return options.stream().collect(Collectors.toMap(OptionEntity::getId, o -> o));
    }

    private EvaluationResult evaluate(List<AnswerRequest> answers,
                                      Map<Long, QuestionEntity> questionMap,
                                      Map<Long, OptionEntity> optionMap) {
        EvaluationResult result = new EvaluationResult();
        OffsetDateTime now = OffsetDateTime.now();

        for (AnswerRequest answer : answers) {
            QuestionEntity question = questionMap.get(answer.getQuestionId());
            OptionEntity selectedOption = optionMap.get(answer.getSelectedOptionId());

            if (!selectedOption.getQuestion().getId().equals(question.getId()))
                throw new IllegalStateException("Option does not belong to question");

            boolean isCorrect = selectedOption.isCorrect();

            UserAnswerEntity userAnswer = new UserAnswerEntity();
            userAnswer.setQuestion(question);
            userAnswer.setSelectedOption(selectedOption);
            userAnswer.setCorrect(isCorrect);
            userAnswer.setAnsweredAt(now);

            result.record(question, userAnswer, isCorrect);
        }

        return result;
    }

    private TestResultResponse buildResponse(EvaluationResult eval) {
        double score = Math.round((eval.getCorrect() * 100.0 / eval.getTotal()) * 100.0) / 100.0;

        Map<String, ByCategoryResponse> byCategory = new HashMap<>();
        List<WeaknessResponse> weaknesses = new ArrayList<>();

        eval.getTotalByCategory().forEach((category, total) -> {
            int correct = eval.getCorrectByCategory().getOrDefault(category, 0);
            int percentage = (correct * 100) / total;

            byCategory.put(category.name(), ByCategoryResponse.builder()
                    .correct(correct).total(total).percentage(percentage).build());

            if (total >= 3 && percentage < 70) {
                weaknesses.add(WeaknessResponse.builder()
                        .category(category.name())
                        .message(category.getWeaknessMessage())
                        .percentage(percentage)
                        .build());
            }
        });

        Map<String, ByCategoryResponse> byDifficulty = new HashMap<>();
        eval.getTotalByDifficulty().forEach((difficulty, total) -> {
            int correct = eval.getCorrectByDifficulty().getOrDefault(difficulty, 0);
            int percentage = (correct * 100) / total;

            byDifficulty.put(difficulty.name(), ByCategoryResponse.builder()
                    .correct(correct).total(total).percentage(percentage).build());
        });

        TestResultResponse response = new TestResultResponse();
        response.setScore(score);
        response.setByCategory(byCategory);
        response.setByDifficulty(byDifficulty);
        response.setWeaknesses(weaknesses);
        return response;
    }

    @Getter
    private static class EvaluationResult {
        private int correct;
        private int total;
        private final Map<Category, Integer> totalByCategory = new HashMap<>();
        private final Map<Category, Integer> correctByCategory = new HashMap<>();
        private final Map<Difficulty, Integer> totalByDifficulty = new HashMap<>();
        private final Map<Difficulty, Integer> correctByDifficulty = new HashMap<>();
        private final List<UserAnswerEntity> userAnswers = new ArrayList<>();

        void record(QuestionEntity question, UserAnswerEntity answer, boolean isCorrect) {
            total++;
            if (isCorrect) correct++;
            totalByCategory.merge(question.getCategory(), 1, Integer::sum);
            totalByDifficulty.merge(question.getDifficulty(), 1, Integer::sum);
            if (isCorrect) {
                correctByCategory.merge(question.getCategory(), 1, Integer::sum);
                correctByDifficulty.merge(question.getDifficulty(), 1, Integer::sum);
            }
            userAnswers.add(answer);
        }
    }

    public TestSessionResponse start(UUID userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Area userArea = user.getArea();

        // --- Bloques PASSAGE ---
        List<PassageEntity> passages = passageRepository.findByArea(userArea);
        if (passages.size() < 3) {
            throw new IllegalStateException("Not enough passages for area " + userArea);
        }

        Collections.shuffle(passages);
        List<PassageEntity> selectedPassages = passages.subList(0, 3);

        List<List<QuestionEntity>> passageBlocks = new ArrayList<>();

        for (PassageEntity passage : selectedPassages) {
            List<QuestionEntity> questions = new ArrayList<>(passage.getQuestions());
            if (questions.size() < 4) {
                throw new IllegalStateException("Passage does not have enough questions");
            }
            Collections.shuffle(questions);
            passageBlocks.add(questions.subList(0, 4));
        }

        // --- Bloques STANDALONE ---
        List<QuestionEntity> preguntasSueltas = questionRepository.findByAreaAndPassageIsNull(userArea);
        if (preguntasSueltas.size() < 8) {
            throw new IllegalStateException("Not enough standalone questions for area " + userArea);
        }

        Collections.shuffle(preguntasSueltas);
        List<QuestionEntity> selectedStandalone = preguntasSueltas.subList(0, 8);

        List<List<QuestionEntity>> standaloneBlocks = new ArrayList<>();
        for (QuestionEntity q : selectedStandalone) {
            standaloneBlocks.add(List.of(q));
        }

        // --- Mezclar bloques ---
        List<List<QuestionEntity>> allBlocks = new ArrayList<>();
        allBlocks.addAll(passageBlocks);
        allBlocks.addAll(standaloneBlocks);
        Collections.shuffle(allBlocks);

        // --- Aplanar bloques en orden y asignar displayOrder ---
        TestSessionEntity session = new TestSessionEntity();
        session.setUser(user);
        session.setStartedAt(OffsetDateTime.now());
        testSessionRepository.save(session);

        List<TestSessionQuestionEntity> sessionQuestions = new ArrayList<>();
        int order = 1;

        for (List<QuestionEntity> block : allBlocks) {
            for (QuestionEntity question : block) {
                TestSessionQuestionEntity tsq = new TestSessionQuestionEntity();
                tsq.setSession(session);
                tsq.setQuestion(question);
                tsq.setDisplayOrder(order++);
                sessionQuestions.add(tsq);
            }
        }

        testSessionQuestionRepository.saveAll(sessionQuestions);

        TestSessionResponse response = new TestSessionResponse();
        response.setSessionId(session.getId());
        return response;
    }

}
