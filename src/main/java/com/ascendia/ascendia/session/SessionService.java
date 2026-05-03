package com.ascendia.ascendia.session;

import com.ascendia.ascendia.common.Area;
import com.ascendia.ascendia.question.*;
import com.ascendia.ascendia.user.UserEntity;
import com.ascendia.ascendia.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final TestSessionQuestionRepository testSessionQuestionRepository;
    private final TestSessionRepository testSessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final PassageRepository passageRepository;
    private final UserRepository userRepository;

    @Transactional
    public FinishSessionResponse finish(FinishSessionRequest request) {

        TestSessionEntity session = testSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getFinishedAt() != null)
            throw new IllegalStateException("Session already finished");

        OffsetDateTime finishedAt = OffsetDateTime.now();
        session.setFinishedAt(finishedAt);
        testSessionRepository.save(session);

        long durationSeconds = ChronoUnit.SECONDS.between(session.getStartedAt(), finishedAt);

        FinishSessionResponse response = new FinishSessionResponse();
        response.setSessionId(session.getId());
        response.setFinished(true);
        response.setFinishedAt(finishedAt);
        response.setDurationSeconds(durationSeconds);
        return response;
    }

    public SessionReviewResponse getReview(UUID sessionId) {

        TestSessionEntity session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getFinishedAt() == null)
            throw new IllegalStateException("Session is not finished yet");

        List<TestSessionQuestionEntity> sessionQuestions =
                testSessionQuestionRepository.findBySessionIdOrderByDisplayOrderAsc(sessionId);

        List<Long> questionIds = sessionQuestions.stream()
                .map(tsq -> tsq.getQuestion().getId())
                .toList();

        Map<Long, QuestionEntity> questionMap = questionRepository
                .findAllWithOptionsByIds(questionIds)
                .stream()
                .collect(Collectors.toMap(QuestionEntity::getId, q -> q));

        Map<Long, UserAnswerEntity> answerMap = userAnswerRepository
                .findBySessionId(sessionId)
                .stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        List<ReviewQuestionResponse> reviewQuestions = new ArrayList<>();
        Map<String, StatEntryResponse> byDifficulty = new LinkedHashMap<>();
        Map<String, StatEntryResponse> byCategory = new LinkedHashMap<>();
        int correct = 0;

        for (TestSessionQuestionEntity tsq : sessionQuestions) {
            Long questionId = tsq.getQuestion().getId();
            QuestionEntity question = questionMap.get(questionId);
            UserAnswerEntity answer = answerMap.get(questionId);

            Long correctOptionId = question.getOptions().stream()
                    .filter(OptionEntity::isCorrect)
                    .map(OptionEntity::getId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No correct option for question " + questionId));

            boolean omitted = answer == null;
            boolean isCorrect = !omitted && answer.isCorrect();

            if (isCorrect) correct++;

            // Detail
            ReviewQuestionResponse q = new ReviewQuestionResponse();
            q.setQuestionId(questionId);
            q.setCorrect(isCorrect);
            q.setOmitted(omitted);
            q.setSelectedOptionId(omitted ? null : answer.getSelectedOption().getId());
            q.setCorrectOptionId(correctOptionId);
            q.setExplanation(question.getExplanation());
            reviewQuestions.add(q);

            // Stats
            String difficulty = question.getDifficulty().name();
            String category = question.getCategory().name();

            byDifficulty.computeIfAbsent(difficulty, k -> new StatEntryResponse()).incrementTotal();
            byCategory.computeIfAbsent(category, k -> new StatEntryResponse()).incrementTotal();

            if (isCorrect) {
                byDifficulty.get(difficulty).incrementCorrect();
                byCategory.get(category).incrementCorrect();
            }
        }

        int totalQuestions = sessionQuestions.size();
        int answered = answerMap.size();
        int omitted = totalQuestions - answered;
        int incorrect = totalQuestions - correct;
        double score = Math.round((correct * 100.0) / totalQuestions);

        SessionStatsResponse stats = new SessionStatsResponse();
        stats.setByCategory(byCategory);
        stats.setByDifficulty(byDifficulty);

        SessionReviewResponse response = new SessionReviewResponse();
        response.setSessionId(sessionId);
        response.setTotalQuestions(totalQuestions);
        response.setAnswered(answered);
        response.setOmitted(omitted);
        response.setCorrect(correct);
        response.setIncorrect(incorrect);
        response.setScore(score);
        response.setQuestions(reviewQuestions);
        response.setStats(stats);
        return response;
    }

    public TestSessionResponse start(StartSessionRequest request) {

        UserEntity user = userRepository.findById(request.getUserId())
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
        session.setMode(request.getMode());
        session.setArea(userArea);
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

    public SessionHistoryResponse getHistory(UUID userId) {

        List<TestSessionEntity> sessions = testSessionRepository.findFinishedSessionsByUserId(userId);

        List<SessionSummaryResponse> summaries = sessions.stream()
                .map(this::mapToSummary)
                .toList();

        SessionHistoryResponse response = new SessionHistoryResponse();
        response.setSessions(summaries);
        return response;
    }

    private SessionSummaryResponse mapToSummary(TestSessionEntity session) {

        List<UserAnswerEntity> answers = userAnswerRepository.findBySessionId(session.getId());

        int correct = (int) answers.stream().filter(UserAnswerEntity::isCorrect).count();
        int total = testSessionQuestionRepository.countBySessionId(session.getId());
        int score = (int) Math.round((correct * 100.0) / total);
        long duration = ChronoUnit.SECONDS.between(session.getStartedAt(), session.getFinishedAt());

        SessionSummaryResponse summary = new SessionSummaryResponse();
        summary.setSessionId(session.getId());
        summary.setMode(session.getMode().name());
        summary.setArea(session.getArea().name());
        summary.setScore(score);
        summary.setCorrect(correct);
        summary.setTotal(total);
        summary.setStartedAt(session.getStartedAt());
        summary.setFinishedAt(session.getFinishedAt());
        summary.setDurationSeconds(duration);
        return summary;
    }

}
