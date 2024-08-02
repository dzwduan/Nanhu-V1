package nanhu

import chisel3._
import nanhu.Parameter
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import simulation.Simulator._
import nanhu.rename._

class RenameTable1_test extends AnyFreeSpec with Matchers {
  "RenameTable1_test" in {
    implicit val p = Parameter()
    val rename  = simulate(new RenameTable1) { dut =>

      def clear() : Unit = {
        dut.io.readPorts.foreach(_.data.poke(0.U))
      }

      def reset() : Unit = {
        dut.io.readPorts.foreach{ r =>
          r.hold := false.B

        }
      }
    }


  }
}
