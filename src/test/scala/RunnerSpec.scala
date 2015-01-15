import org.scalatest.FunSpec

class RunnerSpec extends FunSpec {
  describe("Argument Checker") {
    it("can receive correct arguments") {
      Runner.checkArguments(Array("-p 3000", "-c 10", "-m development"))
    }
  }
}
