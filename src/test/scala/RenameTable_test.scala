package nanhu

import chisel3._
import chisel3.util._
import nanhu.rename._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._

class RenameTable1_test extends AnyFreeSpec with Matchers {
  "RenameTable1_test pass" in {
    implicit val p = Parameter()
    val rename = simulate(new RenameTable) { dut =>
      // val ref = Array.tabulate(p.NRArchRegs)(i => i)
      reset()
      dut.clock.step()

      // 测试hold 只使用一个port

      // write addr 2 without hold
      write(2, 22)
      read_no_hold(2)
      check(22)

      // write addr 3 with hold
      write(3, 33)
      read_next_hold(3)
      check(33)
      dut.clock.step()
      check(33)

      def check(data : Int) : Unit = {
        dut.io.readPorts(0).data.expect(data.U)
      }

      def write(addr: Int, data: Int): Unit = {
        dut.io.specWritePorts(0).wen.poke(true.B)
        dut.io.specWritePorts(0).addr.poke(addr.U)
        dut.io.specWritePorts(0).data.poke(data.U)
        write_step()
      }

      def read_next_hold(addr: Int): Unit = {
        dut.io.readPorts(0).hold.poke(false.B)
        dut.io.readPorts(0).addr.poke(addr.U)
        dut.clock.step()
        dut.io.readPorts(0).addr.poke(0.U)
        dut.io.readPorts(0).hold.poke(true.B)
        read_step()
      }

      def read_no_hold(addr : Int): Unit = {
        dut.clock.step()
        dut.io.readPorts(0).addr.poke(addr.U)
        dut.io.readPorts(0).hold.poke(false.B)
        read_step()
      }

      def write_step(): Unit = {
        dut.clock.step()
        dut.io.specWritePorts(0).wen.poke(false.B)
        dut.io.specWritePorts(0).addr.poke(0.U)
        dut.io.specWritePorts(0).data.poke(0.U)
      }

      def read_step() : Unit = {
        dut.clock.step()
        dut.io.readPorts(0).addr.poke(0.U)
        dut.io.readPorts(0).hold.poke(false.B)
      }

      // dut.clock.step()
      // dut.io.readPorts(0).data.expect(22.U)
      // dut.io.readPorts(0).hold.poke(false.B)

      // // write addr 2 and test hold
      // dut.io.specWritePorts(0).wen.poke(true.B)
      // dut.io.specWritePorts(0).addr.poke(2.U)
      // dut.io.specWritePorts(0).data.poke(44.U)
      // dut.clock.step()
      // dut.io.specWritePorts(0).wen.poke(false.B)
      // dut.io.readPorts(0).addr.poke(2.U)
      // dut.io.readPorts(0).hold.poke(true.B)
      // dut.io.readPorts(0).data.expect(44.U)
      // dut.clock.step()
      // dut.io.readPorts(0).data.expect(44.U)

      // writeTable(1, 1, 11)
      // dut.clock.step()
      // writeTable(2, 2, 22)
      // dut.clock.step()
      // writeTable(3, 3, 33)
      // dut.clock.step()

      // readTable(1, 1)
      // stepDiff()
      // readTable(2, 2)
      // stepDiff()
      // readTable(3, 3)
      // stepDiff()
      // clearWrite()
      // readAll()

      // def testHold() : Unit = {
      //   dut.io.readPorts(0).hold.poke(true.B)
      //   dut.io.readPorts(0).addr.poke(1.U)
      //   checkRead()
      // }

      // def readAll(): Unit = {
      //   clearWrite()
      //   for (i <- 0 until 32) {
      //     dut.io.readPorts(0).addr.poke(i.U)
      //     checkRead()
      //   }
      // }

      // def readTable(port: Int, addr: Int): Unit = {
      //   dut.io.readPorts(port).addr.poke(addr.U)
      // }

      // def stepDiff(): Unit = {
      //   dut.clock.step()
      //   checkRead()
      // }

      // def checkRead(): Unit = {
      //     val addr = dut.io.readPorts(5).addr.peekValue().asBigInt.toInt
      //     dut.clock.step()
      //     dut.io.readPorts(5).data.expect(ref(addr), s"rat[$addr] error")
      // }

      // def writeTable(port: Int, addr: Int, data: Int): Unit = {
      //   dut.io.specWritePorts(port).wen.poke(true.B)
      //   dut.io.specWritePorts(port).addr.poke(addr.U)
      //   dut.io.specWritePorts(port).data.poke(data.U)
      //   ref(addr) = data
      // }

      // def clearWrite(): Unit = {
      //   for (i <- 0 until 32) {
      //     dut.io.specWritePorts(5).wen.poke(true.B)
      //     dut.io.specWritePorts(5).addr.poke(i.U)
      //     dut.io.specWritePorts(5).data.poke(0.U)
      //     dut.clock.step()
      //   }

      //   for (i <- 0 until 32) {
      //     dut.io.specWritePorts(5).wen.poke(false.B)
      //   }

      // }

      def reset(): Unit = {
        dut.clock.step()
        dut.reset.poke(false.B)
        dut.clock.step()
      }
    }

  }
}