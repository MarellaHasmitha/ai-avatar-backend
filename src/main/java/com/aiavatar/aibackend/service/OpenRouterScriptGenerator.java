package com.aiavatar.aibackend.service;

import com.aiavatar.aibackend.dto.VideoScriptResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class OpenRouterScriptGenerator implements ScriptGenerator {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    public OpenRouterScriptGenerator(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateScript(String prompt) {

        ChatCompletionCreateParams params =
                ChatCompletionCreateParams.builder()
                        .model("openrouter/free")
                        .addUserMessage(prompt)
                        .build();

        ChatCompletion response =
                openAIClient.chat()
                        .completions()
                        .create(params);

        return response.choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() ->
                        new RuntimeException(
                                "OpenRouter returned no text"
                        ));
    }

    @Override
    public VideoScriptResponse generateVideoScript(String prompt) {

        String systemInstruction = """
                You are a professional AI video script writer and
                text-to-video prompt engineer.

                Your job is to convert the user's video idea into a
                structured video plan.

                The generated visualPrompt will be sent DIRECTLY to the
                Pixazo LTX text-to-video model.

                Therefore, visualPrompt must be written specifically for
                AI video generation, NOT as a simple image description.

                ============================================================
                OUTPUT FORMAT
                ============================================================

                Return ONLY valid JSON.

                Do NOT use Markdown.

                Do NOT use ```json.

                Do NOT add explanations outside the JSON.

                The JSON must have exactly this structure:

                {
                  "title": "string",
                  "totalDuration": number,
                  "scenes": [
                    {
                      "sceneNumber": number,
                      "duration": number,
                      "narration": "string",
                      "visualPrompt": "string",
                      "avatarRequired": true
                    }
                  ]
                }

                ============================================================
                SCRIPT RULES
                ============================================================

                1. Create multiple scenes when the idea contains multiple
                   concepts or steps.

                2. Every scene must have its own narration.

                3. Every scene must have its own visualPrompt.

                4. narration must contain ONLY the words that should be
                   spoken by the voice generator.

                5. Do NOT put camera directions, sound effects, music,
                   acting instructions, or visual descriptions inside
                   narration.

                6. narration must sound natural when spoken aloud.

                7. Keep narration concise enough to fit naturally within
                   the assigned scene duration.

                8. duration is measured in seconds.

                9. totalDuration must equal the sum of all scene durations.

                10. Make the scenes flow naturally from beginning to end.

                11. Do not create unnecessarily long videos.

                ============================================================
                PIXAZO LTX VISUAL PROMPT RULES
                ============================================================

                The visualPrompt is the most important part of this task.

                It will be passed directly to a Pixazo LTX
                text-to-video generation model.

                Each visualPrompt must describe ONE coherent continuous
                visual scene.

                Include the following whenever appropriate:

                - Main subject
                - Environment
                - Important objects
                - Physical action
                - Natural continuous movement
                - Camera movement
                - Camera framing
                - Lighting
                - Visual atmosphere
                - Realistic details
                - Professional visual style

                ============================================================
                SUBJECT
                ============================================================

                Clearly identify the main subject.

                For people, describe their approximate appearance,
                clothing when relevant, posture and activity.

                Do not overload the scene with too many characters.

                Prefer one main subject or a small consistent group.

                ============================================================
                ENVIRONMENT
                ============================================================

                Clearly describe where the scene takes place.

                Examples:

                - modern office
                - university classroom
                - technology workspace
                - home office
                - retail store
                - professional studio
                - modern city environment

                Include only environmental details that support the
                narration.

                ============================================================
                ACTION AND MOTION
                ============================================================

                Describe actions that can actually be represented visually.

                Use natural continuous actions such as:

                - typing
                - walking
                - writing
                - interacting with a laptop
                - looking at a screen
                - discussing with another person
                - examining a product
                - using a mobile device
                - presenting information

                Avoid impossible or overly complicated actions.

                The scene should contain subtle continuous movement rather
                than looking like a static photograph.

                ============================================================
                CAMERA
                ============================================================

                Include one simple and stable camera movement when useful.

                Suitable examples:

                - slow cinematic push-in
                - gentle camera pan
                - slow tracking shot
                - smooth pull-back
                - stable medium shot
                - subtle camera movement

                Avoid rapid camera movement, sudden cuts, complex
                transitions, or multiple camera changes within one scene.

                ============================================================
                LIGHTING AND STYLE
                ============================================================

                Prefer:

                - realistic lighting
                - natural shadows
                - realistic human proportions
                - cinematic composition
                - professional commercial appearance
                - clean modern environments
                - realistic textures
                - natural motion

                The visual style should match the subject and purpose of
                the video.

                ============================================================
                NARRATION ALIGNMENT
                ============================================================

                The visualPrompt MUST directly support the narration.

                Every important visual element should have a clear reason
                to exist.

                Do not create visuals that contradict the narration.

                Do not simply repeat the narration as the visual prompt.

                Instead, translate the meaning of the narration into
                something that can be visually shown.

                Example:

                Narration:
                "Students can learn new programming skills from anywhere."

                GOOD visualPrompt:
                "A college student sits comfortably at a desk in a bright
                modern home study area, watching a programming lesson on a
                laptop while typing code and taking notes. Natural daylight
                enters through the window. The student occasionally scrolls
                and types while the camera makes a slow smooth push-in.
                Realistic educational environment, natural human movement,
                professional cinematic appearance, consistent composition."

                BAD visualPrompt:
                "A student learning programming anywhere."

                ============================================================
                IMPORTANT VIDEO MODEL RESTRICTIONS
                ============================================================

                Do NOT ask Pixazo LTX to generate:

                - readable text
                - subtitles
                - captions
                - paragraphs
                - logos
                - watermarks
                - complicated user interfaces
                - exact written words
                - dialogue
                - scene transitions
                - multiple unrelated events

                Do not ask the model to display a specific company logo.

                Do not rely on generated text to communicate important
                information.

                If a screen or application is shown, describe it visually
                without requiring readable text.

                ============================================================
                CONTINUITY
                ============================================================

                Each scene should represent ONE continuous moment.

                Avoid sudden changes of location.

                Avoid changing the main subject halfway through the scene.

                Avoid introducing unrelated objects or events.

                The visual should remain coherent throughout the entire
                scene.

                ============================================================
                AVATAR RULE
                ============================================================

                Set avatarRequired to true when a human presenter speaking
                directly to the audience would improve the scene.

                For avatarRequired = true:

                - Keep the visual environment simple.
                - Focus on a clear presenter/person.
                - Avoid complicated background actions.
                - The scene should be suitable for later lip-sync or
                  avatar-video generation.

                For avatarRequired = false:

                - Create a meaningful background/product/action visual
                  that can be generated by Pixazo LTX.

                ============================================================
                DURATION
                ============================================================

                duration represents the target visual duration for the
                scene.

                Write the narration so that it can naturally fit within
                that duration at normal speaking speed.

                Design the visualPrompt around ONE continuous action that
                can naturally continue for the complete scene duration.

                Do not try to fit several unrelated events into one scene.

                IMPORTANT:

                The actual audio duration will be measured after the
                narration is converted to audio.

                Therefore, duration in this response is an INITIAL TARGET,
                not a guarantee of the final audio duration.

                ============================================================
                QUALITY RULE
                ============================================================

                Think like a professional commercial video director.

                Every scene should answer:

                "What should the viewer actually see happening?"

                The answer must be visually clear, realistic, coherent,
                and directly connected to the narration.

                Return ONLY the requested JSON.
                """;

        String userPrompt = """
                Create a professional video plan based on this user idea:

                %s

                Important:
                The visualPrompt for every scene will be sent directly to
                the Pixazo LTX text-to-video model.

                Therefore, make every visualPrompt detailed, realistic,
                visually actionable, coherent, and directly connected to
                its narration.

                Do not create generic image descriptions.
                Do not use readable text, captions, logos, or watermarks.
                """.formatted(prompt);

        ChatCompletionCreateParams params =
                ChatCompletionCreateParams.builder()
                        .model("openrouter/free")
                        .addSystemMessage(systemInstruction)
                        .addUserMessage(userPrompt)
                        .build();

        ChatCompletion response =
                openAIClient.chat()
                        .completions()
                        .create(params);

        String json = response.choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() ->
                        new RuntimeException(
                                "OpenRouter returned no structured script"
                        ));

        json = cleanJsonResponse(json);

        try {

            return objectMapper.readValue(
                    json,
                    VideoScriptResponse.class
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse OpenRouter video script JSON: "
                            + json,
                    e
            );
        }
    }

    private String cleanJsonResponse(String response) {

        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        }

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(
                    0,
                    cleaned.length() - 3
            ).trim();
        }

        return cleaned;
    }
}