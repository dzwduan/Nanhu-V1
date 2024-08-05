package nanhu

import chisel3._
import chisel3.util._
import nanhu.Parameter
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._
import nanhu.rename._

class RenameTable1_test extends AnyFreeSpec with Matchers {
  "RenameTable1_test" in {
    implicit val p = Parameter()
    val rename  = simulate(new RenameTable1) { dut =>
      val ref = VecInit.fill(p.RenameWidth * 3)(0.U)
      reset()
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      //writeTable(0, 1, 11)
      writeTable(1, 1, 11)
      writeTable(2, 2, 22)
      writeTable(3, 3, 33)

      readTable(1, 1)
      readTable(2, 2)
      readTable(3, 3)
      stepDiff()
      clearWrite()
      readAll()


      def readAll() : Unit = {
        clearWrite()
        for ( i <- 0 until 32) {
          dut.io.readPorts(0).addr.poke(i)
          checkRead()
        }
      }


      def readTable(port : Int, addr : Int) : Unit = {
        dut.io.readPorts(port).addr.poke(addr)
      }

      def stepDiff() : Unit = {
        dut.clock.step()
        checkRead()
      }

      def checkRead() : Unit = {
        dut.io.readPorts.foreach( r => {
          val addr = r.addr.peekValue().asBigInt.toInt
          r.data.expect(ref(addr), s"rat[$addr] error")
        })
      }

      def writeTable(port : Int, addr : Int, data : Int) : Unit = {
        dut.io.specWritePorts(port).wen.poke(true.B)
        dut.io.specWritePorts(port).addr.poke(addr.U)
        dut.io.specWritePorts(port).data.poke(data.U)
        ref(addr) := data.U
      }

      def clearWrite() : Unit = {
        dut.io.specWritePorts.foreach(_.data.poke(0.U))
      }

      def clear() : Unit = {
        dut.io.readPorts.foreach(_.data.poke(0.U))
      }

      def reset() : Unit = {
        clear()
        reset()
        dut.clock.step()
        dut.reset.poke(false.B)
      }
    }


  }
}
