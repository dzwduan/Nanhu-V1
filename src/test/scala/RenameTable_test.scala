package nanhu

import chisel3._
import chisel3.util._
import nanhu.rename._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._

class RenameTable_test extends AnyFreeSpec with Matchers {
  "RenameTable_test pass" in {
    implicit val p = Parameter()
    val rename = simulate(new RenameTable) { dut =>
      // val ref = Array.tabulate(p.NRArchRegs)(i => i)
      reset()
      dut.clock.step()

      // 向 port 0 addr 2 写入data 22
      write(0, 2, 22)
      dut.clock.step()
      write_step(0)
      read(0, 2)
      check(0, 22)
      read_step(0)
      dut.clock.step()

      // 同一时刻向port 0 ,port 1, 同一地址 写入不同的data
      write(0, 3, 33)
      write(1, 3, 44)
      dut.clock.step()
      write_step(0)
      write_step(1)
      read(0, 3)
      check(0, 44)
      read_step(0)

     
      def check(port : Int, data : Int) : Unit = {
        dut.io.readPorts(port).data.expect(data.U)
      }

      def write(port : Int, addr: Int, data: Int): Unit = {
        dut.io.specWritePorts(port).wen.poke(true.B)
        dut.io.specWritePorts(port).addr.poke(addr.U)
        dut.io.specWritePorts(port).data.poke(data.U)
      }

      def read(port : Int, addr : Int): Unit = {
        dut.io.readPorts(port).addr.poke(addr.U)
        // read_step(port)
      }

      def write_step(port : Int): Unit = {
        dut.io.specWritePorts(port).wen.poke(false.B)
        dut.io.specWritePorts(port).addr.poke(0.U)
        dut.io.specWritePorts(port).data.poke(0.U)
      }

      def read_step(port : Int) : Unit = {
        dut.clock.step()
        dut.io.readPorts(port).addr.poke(0.U)
      }

      def reset(): Unit = {
        dut.clock.step()
        dut.reset.poke(false.B)
        dut.clock.step()
      }
    }

  }
}
