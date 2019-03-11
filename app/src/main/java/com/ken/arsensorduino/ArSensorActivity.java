/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ken.arsensorduino;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.ken.arsensorduino.util.ColorUtil;
import com.ken.arsensorduino.util.MathUtil;

public class ArSensorActivity extends AppCompatActivity {

  private ArFragment arFragment;
  private final AnchorNode anchorNode = new AnchorNode();
  private final SerialReader serialReader = new SerialReader();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar_sensor);

    serialReader.start(this);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
    arFragment.getArSceneView().getScene().addChild(anchorNode);
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
    Vector3 position = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();

    plotPoint(position, latestValue);
  }

  private static final int MIN_VALUE = 80;
  private static final int MAX_VALUE = 110;
  private static final float MIN_HUE = 1 / 3f;
  private static final float MAX_HUE = 0;
  private static final float RADIUS = 0.03f;

  private void plotPoint(Vector3 position, float value) {
    float hue = MathUtil.map(value, MIN_VALUE, MAX_VALUE, MIN_HUE, MAX_HUE);
    Color color = new Color(ColorUtil.HSBtoRGB(hue, 1, 1));

    MaterialFactory.makeOpaqueWithColor(this, color).thenAccept(material -> {
      Node node = new Node();
      node.setRenderable(ShapeFactory.makeSphere(
          RADIUS,
          position,
          material));
      anchorNode.addChild(node);
    });
  }
}
