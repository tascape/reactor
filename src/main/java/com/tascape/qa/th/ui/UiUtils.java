/*
 * Copyright 2016 Nebula Bay.
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
package com.tascape.qa.th.ui;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UiUtils {
    private static final Logger LOG = LoggerFactory.getLogger(UiUtils.class);

    public static List<Image> getAvailableIconImages() {
        List<Image> images = new ArrayList<>();
        LOG.debug("load Grill-48.png");
        URL imgURL = UiUtils.class.getResource("Grill-48.png");
        images.add(new ImageIcon(imgURL, "").getImage());
        LOG.debug("load Grill-96.png");
        imgURL = UiUtils.class.getResource("Grill-96.png");
        images.add(new ImageIcon(imgURL, "").getImage());
        return images;
    }
}
