package com.ascendia.ascendia.question;

import com.ascendia.ascendia.common.Area;
import com.ascendia.ascendia.session.TestSessionQuestionEntity;
import com.ascendia.ascendia.session.TestSessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final TestSessionQuestionRepository testSessionQuestionRepository;

    public SessionQuestionsResponse getSessionQuestions(UUID sessionId) {

        List<TestSessionQuestionEntity> sessionQuestions =
                testSessionQuestionRepository.findBySessionIdOrderByDisplayOrderAsc(sessionId);

        if (sessionQuestions.isEmpty()) {
            throw new IllegalArgumentException("Session not found or has no questions");
        }

        List<BlockResponse> blocks = new ArrayList<>();

        for (TestSessionQuestionEntity tsq : sessionQuestions) {
            QuestionEntity question = tsq.getQuestion();
            QuestionResponse questionResponse = mapToResponse(question);

            if (question.getPassage() != null) {
                UUID passageId = question.getPassage().getId();

                BlockResponse lastBlock = blocks.isEmpty() ? null : blocks.getLast();

                boolean canAppend = lastBlock != null
                        && "PASSAGE".equals(lastBlock.getType())
                        && lastBlock.getPassage().getId().equals(passageId);

                if (canAppend) {
                    lastBlock.getQuestions().add(questionResponse);
                } else {
                    BlockResponse newBlock = new BlockResponse();
                    newBlock.setType("PASSAGE");
                    newBlock.setPassage(mapPassageToResponse(question.getPassage()));
                    newBlock.setQuestions(new ArrayList<>(List.of(questionResponse)));
                    blocks.add(newBlock);
                }

            } else {
                BlockResponse newBlock = new BlockResponse();
                newBlock.setType("STANDALONE");
                newBlock.setPassage(null);
                newBlock.setQuestions(new ArrayList<>(List.of(questionResponse)));
                blocks.add(newBlock);
            }
        }

        Area area = sessionQuestions.getFirst()
                .getSession()
                .getUser()
                .getArea();

        SessionQuestionsResponse response = new SessionQuestionsResponse();
        response.setSessionId(sessionId);
        response.setArea(area.name());
        response.setBlocks(blocks);

        return response;
    }

    private PassageResponse mapPassageToResponse(PassageEntity passage) {
        PassageResponse res = new PassageResponse();
        res.setId(passage.getId());
        res.setTitle(passage.getTitle());
        res.setText(passage.getText());
        return res;
    }

    private QuestionResponse mapToResponse(QuestionEntity q) {
        QuestionResponse res = new QuestionResponse();

        res.setId(q.getId());
        res.setText(q.getText());
        res.setCategory(q.getCategory().name());
        res.setDifficulty(q.getDifficulty().name());

        List<OptionResponse> options = q.getOptions().stream()
                .map(o -> {
                    OptionResponse opt = new OptionResponse();
                    opt.setId(o.getId());
                    opt.setText(o.getText());
                    return opt;
                })
                .toList();

        res.setOptions(options);

        return res;
    }
}
