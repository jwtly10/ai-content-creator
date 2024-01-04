package com.jwtly10.aicontentgenerator.service.OpenAI;

import com.jwtly10.aicontentgenerator.model.Gender;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OpenAPIService {

    private final OpenAiService service;

    public OpenAPIService(OpenAiService service) {
        this.service = service;
    }

    /**
     * Improve content by correcting grammar mistakes
     *
     * @param content Content to improve
     * @return Improved content
     */
    public String improveContent(String content) throws OpenAiHttpException {
        log.info("Improving content via OpenAPI");
        String instructions = "Given the following reddit post, correct grammar mistakes. Don't alter curse words or swearing. Replace slashes and " +
                "dashes with the appropriate word.Remove dashes between words like high-end. Add punctuation as necessary for smooth speech flow. Only respond " +
                "with the modified (or unmodified if no changes were made) text. Do not include any other information.";
        ChatMessage responseMessage = getResponseMessage(List.of(instructions, content));
        log.debug("Original content: {}", content);
        log.debug("Improved content: {}", responseMessage.getContent());
        log.info("Content improved successfully");
        return responseMessage.getContent();
    }

    /**
     * Determine gender from given content
     *
     * @param content Content to determine gender
     * @return Gender of content
     */
    public Gender determineGender(String content) {
        log.info("Determining gender");
        String instructions = "From the given text, determine the poster's gender. Use the context provided by the text. " +
                "If the gender is ambiguous, reply with the most probable gender. Respond with a single letter: 'M' for Male or 'F' for Female.";

        ChatMessage responseMessage = getResponseMessage(List.of(instructions, content));

        log.info("Determined gender: {}", responseMessage.getContent());
        if (responseMessage.getContent().equalsIgnoreCase("M")) {
            return Gender.MALE;
        } else if (responseMessage.getContent().equalsIgnoreCase("F")) {
            return Gender.FEMALE;
        } else {
            log.info("OPEN API responded with something else: {}. Defaulting to male", responseMessage.getContent());
            return Gender.MALE;
        }
    }

    /**
     * Get response message from given messages
     *
     * @param messages Messages to get response from
     * @return Response message
     */
    private ChatMessage getResponseMessage(List<String> messages) throws OpenAiHttpException {
        List<ChatMessage> chatMessages = new ArrayList<>();
        for (String message : messages) {
            chatMessages.add(new ChatMessage(ChatMessageRole.USER.value(), message));
        }

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(chatMessages)
                .model("gpt-3.5-turbo")
                .build();

        return service.createChatCompletion(completionRequest).getChoices().get(0).getMessage();
    }
}
