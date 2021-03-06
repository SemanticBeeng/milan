package com.amazon.milan.lang

import com.amazon.milan.serialization.ScalaObjectMapper
import com.amazon.milan.test.IntRecord
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.junit.Assert._
import org.junit.{After, Before, Test}


@Test
class TestStreamGraph {
  @Before
  def before(): Unit = {
    Configurator.setRootLevel(Level.DEBUG)
  }

  @After
  def after(): Unit = {
    Configurator.setRootLevel(Level.INFO)
  }

  @Test
  def test_StreamGraph_SerializeAndDeserialize_ReturnsEquivalentGraph(): Unit = {
    val input = Stream.of[IntRecord]
    val mapped = input.map(((r: IntRecord) => r) as "record")

    val graph = new StreamGraph(mapped)

    val mapper = new ScalaObjectMapper()
    val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(graph)

    // Parse the json into an array, just to check the correct number of streams are in the json.
    val parsed = mapper.readValue[StreamGraph](json, classOf[StreamGraph])

    assertEquals(graph, parsed)
  }

  @Test
  def test_StreamGraph_GetStreams_WithOneStreamAddedWithOneInput_ReturnsBothStreamNodesWithoutReferences(): Unit = {
    val input = Stream.of[IntRecord]
    val mapped = input.map(((r: IntRecord) => r) as "record")

    val graph = new StreamGraph(mapped)

    val graphStreams = graph.getStreams.toList.sortBy(_.nodeId)
    val originalStreams = List(input, mapped).map(_.node).sortBy(_.nodeId)
    assertEquals(originalStreams, graphStreams)
  }

  @Test
  def test_StreamGraph_GetStreams_AfterDeserialization_WithOneStreamAddedWithOneInput_ReturnsBothStreamNodesWithoutReferences(): Unit = {
    val input = Stream.of[IntRecord]
    val mapped = input.map(((r: IntRecord) => r) as "record")

    val graph = new StreamGraph(mapped)

    val mapper = new ScalaObjectMapper()
    val json = mapper.writeValueAsString(graph)

    val parsedGraph = mapper.readValue[StreamGraph](json, classOf[StreamGraph])

    val graphStreams = parsedGraph.getStreams.toList.sortBy(_.nodeId)
    val originalStreams = List(input, mapped).map(_.node).sortBy(_.nodeId)
    assertEquals(originalStreams, graphStreams)
  }
}
