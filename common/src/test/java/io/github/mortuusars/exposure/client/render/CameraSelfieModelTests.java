package io.github.mortuusars.exposure.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CameraSelfieModelTests {
    @Test
    void thirdPersonSelfiePartsUseTheSameDisplayTransformsAsTheCameraBody() throws Exception {
        for (String hand : List.of("thirdperson_righthand", "thirdperson_lefthand")) {
            JsonObject camera = displayTransform("item/camera_selfie", hand);
            for (String part : List.of("selfie_stick", "lens_selfie", "lens_gold_selfie",
                    "flash_selfie", "flash_gold_selfie")) {
                assertEquals(camera, displayTransform("item/camera_parts/" + part, hand), part + " " + hand);
            }
        }
    }

    private static JsonObject displayTransform(String model, String hand) throws Exception {
        String path = "/assets/exposure/models/" + model + ".json";
        try (var stream = CameraSelfieModelTests.class.getResourceAsStream(path)) {
            assertNotNull(stream, path);
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject display = json.getAsJsonObject("display");
            if (display != null && display.has(hand)) {
                return display.getAsJsonObject(hand);
            }
            assertNotNull(json.get("parent"), path + " lacks a display transform for " + hand);
            return displayTransform(json.get("parent").getAsString().substring("exposure:".length()), hand);
        }
    }
}
