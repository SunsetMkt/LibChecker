package android.view;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(SurfaceControl.Transaction.class)
public class SurfaceControlTransactionHidden {
  public SurfaceControl.Transaction setBackgroundBlurRadius(SurfaceControl sc, int radius) {
    throw new RuntimeException("Stub");
  }
}
