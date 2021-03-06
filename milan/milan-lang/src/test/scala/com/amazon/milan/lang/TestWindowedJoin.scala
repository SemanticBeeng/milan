package com.amazon.milan.lang

import java.time.Duration

import com.amazon.milan.program.{ApplyFunction, FunctionDef, FunctionReference, MapRecord, SelectField, SelectTerm, WindowedLeftJoin}
import com.amazon.milan.test.DateIntRecord
import org.junit.Test


object TestWindowedJoin {
  def sumValues(records: Iterable[DateIntRecord]): Int = {
    records.map(_.i).sum
  }
}


@Test
class TestWindowedJoin {
  @Test
  def test_WindowedJoin_Apply_ProducesExpectedExpression(): Unit = {
    val left = Stream.of[DateIntRecord]
    val right = Stream.of[DateIntRecord]

    val rightWindowed = right.tumblingWindow(r => r.dateTime, Duration.ofMinutes(60), Duration.ZERO)

    val joined = left.leftJoin(rightWindowed)
    val output = joined.apply((leftRecord, rightRecords) => DateIntRecord(leftRecord.dateTime, TestWindowedJoin.sumValues(rightRecords)))

    val MapRecord(mapSource, FunctionDef(List("leftRecord", "rightRecords"), mapFunctionBody)) = output.node.getStreamExpression
    val ApplyFunction(FunctionReference("com.amazon.milan.test.DateIntRecord", "apply"), recordArgs, _) = mapFunctionBody
    val List(SelectField(SelectTerm("leftRecord"), "dateTime"), ApplyFunction(FunctionReference("com.amazon.milan.lang.TestWindowedJoin", "sumValues"), List(SelectTerm("rightRecords")), _)) = recordArgs
    val WindowedLeftJoin(_, _) = mapSource
  }
}
