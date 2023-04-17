package jetbrains.buildServer.nuget.tests.util;

import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

public class TCJMockUtils {

  public static Mockery createInstance() {
    final Synchroniser synchroniser = new Synchroniser();

    return new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);

      //setThreadingPolicy(new SingleThreadedPolicyAvoidingFinaliseProblems());
      setThreadingPolicy(synchroniser);
    }};
  }
}
