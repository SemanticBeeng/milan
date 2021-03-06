package com.amazon.milan.program

import java.time.Instant

import com.amazon.milan.lang.aggregation._
import com.amazon.milan.test.Tuple3Record
import com.amazon.milan.typeutil.{TypeDescriptor, types}
import org.junit.Assert._
import org.junit.Test


@Test
class TestTypeChecker {
  @Test
  def test_TypeChecker_TypeCheck_MapFieldsUniqueByTumblingWindow(): Unit = {
    val inputType = TypeDescriptor.of[Tuple3Record[Instant, Int, Int]]
    val input = new Ref("input", "input", types.stream(inputType))

    val dateExtractor = Tree.fromFunction((r: Tuple3Record[Instant, Int, Int]) => r.f0)
    val window = new TumblingWindow(input, dateExtractor, Duration(1000), Duration.ZERO, "window", "window")

    val uniqueKeyFunc = Tree.fromFunction((r: Tuple3Record[Instant, Int, Int]) => r.f1)
    val uniqueBy = new UniqueBy(window, uniqueKeyFunc, "unique", "unique")

    val field1 = FieldDefinition("t", Tree.fromFunction((i: Instant, r: Tuple3Record[Instant, Int, Int]) => i))
    val field2 = FieldDefinition("i", Tree.fromFunction((i: Instant, r: Tuple3Record[Instant, Int, Int]) => max(r.f2)))
    val map = new MapFields(uniqueBy, List(field1, field2), "map", "map")

    val inputNodeTypes = Map("input" -> types.stream(inputType))
    TypeChecker.typeCheck(map, inputNodeTypes)

    val expectedType = types.stream(TypeDescriptor.createNamedTuple(List(("t", types.Instant), ("i", types.Int))))
    assertEquals(expectedType, map.tpe)
  }
}
