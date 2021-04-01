package ddb.debugger

import ddb.debugger.model.Step
import org.apache.daffodil.debugger.Debugger
import org.apache.daffodil.processors.parsers.{PState, Parser}
import zio.{Queue, ZIO}

object MyDebugger {
}

class MyDebugger(cmd: Queue[Command[_]], ev: Queue[Event]) extends Debugger {
  val rt = zio.Runtime.default

  override def init(state: PState, processor: Parser): Unit = println("[init]")
  override def fini(processor: Parser): Unit = println("[fini]")


  override def startElement(state: PState, processor: Parser): Unit = step(state)
  override def endElement(state: PState, processor: Parser): Unit = step(state)

  def step(state: PState) = {
    rt.unsafeRunSync(cmd.take.flatMap {
      case s@Step(_) => s
        .run(state)
        .flatMap {
          case e: MultiEvent => ev.offerAll(e.events())
          case e => ev.offer(e)
        }
      case _ => ZIO.succeed()
    })
  }
}