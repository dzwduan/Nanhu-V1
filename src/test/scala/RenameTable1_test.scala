package nanhu

import chisel3._
import chisel3.util._
import nanhu.Parameter
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._
import nanhu.rename._

class RenameTable1_test extends AnyFreeSpec with Matchers {
  "RenameTable1_test pass" in {
    implicit val p = Parameter()
    val rename = simulate(new RenameTable1) { dut =>
      val ref = Array.tabulate(32)(_ => 0)
      reset()
      dut.clock.step()

      writeTable(1, 1, 11)
      dut.clock.step()
      writeTable(2, 2, 22)
      dut.clock.step()
      writeTable(3, 3, 33)
      dut.clock.step()

      readTable(1, 1)
      stepDiff()
      readTable(2, 2)
      stepDiff()
      readTable(3, 3)
      stepDiff()
      clearWrite()
      readAll()

      def readAll(): Unit = {
        clearWrite()
        for (i <- 0 until 32) {
          dut.io.readPorts(0).addr.poke(i.U)
          checkRead()
        }
      }

      def readTable(port: Int, addr: Int): Unit = {
        dut.io.readPorts(port).addr.poke(addr.U)
      }

      def stepDiff(): Unit = {
        dut.clock.step()
        checkRead()
      }

      def checkRead(): Unit = {
          val addr = dut.io.readPorts(5).addr.peekValue().asBigInt.toInt
          dut.clock.step()
          dut.io.readPorts(5).data.expect(ref(addr), s"rat[$addr] error")
      }

      def writeTable(port: Int, addr: Int, data: Int): Unit = {
        dut.io.specWritePorts(port).wen.poke(true.B)
        dut.io.specWritePorts(port).addr.poke(addr.U)
        dut.io.specWritePorts(port).data.poke(data.U)
        ref(addr) = data
      }

      def clearWrite(): Unit = {
        for (i <- 0 until 32) {
          dut.io.specWritePorts(5).wen.poke(true.B)
          dut.io.specWritePorts(5).addr.poke(i.U)
          dut.io.specWritePorts(5).data.poke(0.U)
          dut.clock.step()
        }
        
        for (i <- 0 until 32) {
          dut.io.specWritePorts(5).wen.poke(false.B)
        }

        
      }


      def reset(): Unit = {
        dut.clock.step()
        dut.reset.poke(false.B)
        dut.clock.step()
        dut.clock.step()
      }
    }

  }
}
