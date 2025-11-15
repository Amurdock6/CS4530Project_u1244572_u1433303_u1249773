package cs4530.u1433303.cs4530drawingapplication

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VisionParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parseLocalizedObjectAnnotations_andLabels() {
        val sample = """
            {
              "responses": [
                {
                  "labelAnnotations": [
                    { "description": "Dog", "score": 0.88 }
                  ],
                  "localizedObjectAnnotations": [
                    {
                      "name": "Dog",
                      "score": 0.91,
                      "boundingPoly": {
                        "normalizedVertices": [
                          {"x": 0.10, "y": 0.20},
                          {"x": 0.30, "y": 0.20},
                          {"x": 0.30, "y": 0.60},
                          {"x": 0.10, "y": 0.60}
                        ]
                      }
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString<AnnotateImageResponse>(sample)
        assertEquals(1, parsed.responses.size)

        val resp = parsed.responses.first()
        val labels = resp.labelAnnotations
        assertNotNull(labels)
        assertEquals(1, labels!!.size)
        assertEquals("Dog", labels.first().description)
        assertEquals(0.88f, labels.first().score)

        val objs = resp.localizedObjectAnnotations
        assertNotNull(objs)
        assertEquals(1, objs!!.size)
        assertEquals("Dog", objs.first().name)
        assertEquals(0.91f, objs.first().score)
        assertEquals(4, objs.first().boundingPoly.normalizedVertices.size)
    }
}

