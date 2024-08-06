package nanhu

import chisel3._
import chisel3.util._
import nanhu.rename._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._

class RefCounter_test extends AnyFreeSpec with Matchers {
  "RefCounter_test pass" in {
    implicit val p = Parameter()
    val res = simulate(new RefCounter()) { dut =>
      //val ref = Array.tabulate(p.NRPhyRegs)(i => if (i < p.NRArchRegs) 1 else 0)

      reset()
//cycle 1 : allocate 1 , 2
      allocate(0, 1)
      allocate(1, 2)
      allocate_step(0,1)
// cycle 2 : allocate 2 , 3
      allocate(0, 2)
      allocate(1, 3)
      allocate_step(0,1)
//cycle 3 : deallocate 4 , 5
      allocate(0, 4)
      allocate(1, 5)
      allocate_step(0,1)
      dut.clock.step(4)

// cycle 8 : deallocate 1 , 2
      deallocate(0, 1)
      deallocate(1, 2)
      deallocate_step(0,1)
// cycle 9 : deallocate 2 , 3
      deallocate(0, 2)
      deallocate(1, 3)
      deallocate_step(0,1)
// cycle 10 : deallocate 4 , 5
      deallocate(0, 4)
      deallocate(1, 5)
      check_free(0, true, 1)
       deallocate_step(0,1)
// cycle 11 : check 2 3
      check_free(0, true, 2)
      check_free(1, true, 3)
      dut.clock.step()
// cycle 12 : check 4 5
      check_free(0, true, 4)
      check_free(1, true, 5)
      dut.clock.step()

      def allocate(port: Int, idx: Int): Unit = {
        dut.io.allocate(port).valid.poke(true.B)
        dut.io.allocate(port).bits.poke(idx.U)
      }

      def deallocate(port: Int, idx: Int): Unit = {
        dut.io.deallocate(port).valid.poke(true.B)
        dut.io.deallocate(port).bits.poke(idx.U)
      }

      def check_free(port: Int, v: Boolean, n: Int): Unit = {
        dut.freeregs(port).valid.expect(v.B)
        dut.freeregs(port).bits.expect(n.U)
      }

      def allocate_step(port0: Int, port1: Int): Unit = {
        dut.clock.step()
        dut.io.allocate(port0).valid.poke(false.B)
        dut.io.allocate(port1).valid.poke(false.B)
      }

      def deallocate_step(port0: Int, port1: Int): Unit = {
        dut.clock.step()
        dut.io.deallocate(port0).valid.poke(false.B)
        dut.io.deallocate(port1).valid.poke(false.B)
      }

      def reset(): Unit = {
        dut.reset.poke(true.B)
        dut.clock.step()
        dut.reset.poke(false.B)
        dut.clock.step()
      }

    }
  }
}
