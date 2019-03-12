package com.ken.arsensorduino;

import static com.ken.arsensorduino.util.MathUtil.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.ken.arsensorduino.util.ColorUtil;

public class ArSensorActivity extends AppCompatActivity {

  private ArFragment arFragment;
  private final SerialReader serialReader = new SerialReader();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar_sensor);

    serialReader.start(this);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
  }

  private void onUpdate(FrameTime ft) {
    Frame frame = arFragment.getArSceneView().getArFrame();
    if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
      return;
    }

    Integer latestValue = serialReader.fetchLatestValue();
    if (latestValue == null) {
      return;
    }
    plotPoint(latestValue);
  }

  private void plotPoint(float value) {
    // センサの値（80~110）を色相（緑~赤)にマッピングする。
    Color color = ColorUtil.fromHue(map(value, 80, 110, 1 / 3f, 0f));

    MaterialFactory.makeOpaqueWithColor(this, color).thenAccept(material -> {
      Node node = new Node();
      node.setRenderable(ShapeFactory.makeSphere(
          /* radius= */ 0.01f,
          /* position= */ arFragment.getArSceneView().getScene().getCamera().getWorldPosition(),
          material));
      arFragment.getArSceneView().getScene().addChild(node);
    });
  }
}
