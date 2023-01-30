# Video Feedback

Video-feedback fractal hobby code.

[Browsing links here](https://michaelerule.github.io/videofeedback/)

### Project contents

This repository contains the following projects:

#### Working
 - Javascript CPU examples
 - Jython examples
 - Jython video perceptron reimplementation (buggy, but surprisingly performant)
 
#### Incomplete
 - Javascript CPU rewrite (serious user interface bugs)
 - Java Perceptron (obsolete legacy code; unresolved instability issues)
 - Javascript webgl: coming soon, see webgpgpu project examples
 
#### Future
 - Documentation
 - Feedback tutorial
 - Collected renderings
 
### Historical note

This is loosely descended from a feedback project in Java, termed "Perceptron", that began circa 2007. Since then, several implementations of video feedback have emerged elsewhere. This project contains more recent attempts to "reboot" the video-feedback rendering techniques, or to demonstrate it in other languages.

### Video feedback fractals

The video feedback rendering approach is efficient: it involves scanning over an image "display" buffer and pulling in geometrically-transformed pixels from a separate "camera" buffer, to simulate a camera pointed at a screen displaying the camera's own feed (typically with some additional geometric or color-space effects added). Small adjustements in the geometric transformations and color filters can lead to striking and beautiful effects, and it is the hope that the examples collected in this project may spur further creativity and community interest in the technique.

### License

Unless otherwise specified, media, text, and rendered outputs are licensed under the [Creative Commons Attribution Share Alike 4.0 license](https://choosealicense.com/licenses/cc-by-sa-4.0/) (CC BY-SA 4.0). Source code is licensed under the [GNU General Public License version 3.0](https://www.gnu.org/copyleft/gpl.html) (GPLv3). The CC BY-SA 4.0 is [one-way compatible](https://creativecommons.org/compatiblelicenses) with the GPLv3 license. 
This project is published in the spirit (but not the letter) of the [tongue-and-cheek CRAPL license for academic code](http://matt.might.net/articles/crapl/CRAPL-LICENSE.txt). This is not a legal license as it contains internal inconsistencies (like forbidding users to exist). However, it cautions that this project has not been reviewed or prepared for use by the general public.




