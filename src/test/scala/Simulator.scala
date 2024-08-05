package simulation

import chisel3._
import chisel3.simulator._
import svsim.verilator.Backend.CompilationSettings.TraceStyle
import svsim.{CommonCompilationSettings, verilator}


/** Provides a simple API for "ephemeral" invocations (where you don't care about the artifacts after the invocation completes) to
 * simulate Chisel modules. To keep things really simple, `EphemeralSimulator` simulations can only be controlled using the
 * peek/poke API, which provides enough control while hiding some of the lower-level svsim complexity.
 *
 * @example
 * {{{
 * import chisel3.simulator.EphemeralSimulator._
 * ...
 * simulate(new MyChiselModule()) { module => ... }
 * }}}
 */
object Simulator extends PeekPokeAPI {
  def simulate[T <: RawModule](
                                module: => T
                              )(body: (T) => Unit
                              ): Unit = {
    //    makeSimulator.simulate(module)({ module => body(module.wrapped) }).result
    makeSimulator.simulate(module)({ module =>
      module.controller.setTraceEnabled(true)
      body(module.wrapped)
    }).result
  }

  private class DefaultSimulator(val workspacePath: String) extends SingleBackendSimulator[verilator.Backend] {
    val backend = verilator.Backend.initializeFromProcessEnvironment()
    val tag = "default"
    val commonCompilationSettings = CommonCompilationSettings()
    val backendSpecificCompilationSettings = verilator.Backend.CompilationSettings(traceStyle = Option(TraceStyle.Vcd()))
    // Try to clean up temporary workspace if possible
    //    sys.addShutdownHook {
    //      (new Directory(new File(workspacePath))).deleteRecursively()
    //    }
  }

  private def makeSimulator: DefaultSimulator = {
    // TODO: Use ProcessHandle when we can drop Java 8 support
    // val id = ProcessHandle.current().pid().toString()
    val id = java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
    val className = getClass().getName().stripSuffix("$")
    //    new DefaultSimulator(Files.createTempDirectory(s"${className}_${id}_").toString)
    new DefaultSimulator("./build")
  }
}
