# Video Feedback

Simulated video feedback is a simple ways to generate responsive real-time fractals and other visual effects. This repository collects small examples constructed over the years.

### Disclaimer

This code is uploaded to Github without curation or documentation in the hope
that some portions of it may be found useful. If there is community interest
in any portion or file within this project, please open an issue and the 
project maintainers will work to improve documentation and usability. See also the License section below.

### Project contents and objective

At first, this project will aggregate several different implementations of video feedback rendering. Over time, the goal is to refactor and organize these examples, and to provide supplemental documentation outlining the rendering technique.

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

This is loosely descended from a feedback project in Java, termed "Perceptron", that began circa 2007. Due to the demands of video feedback rendering, official platform-specific builds of the Java runtime environment were required to get acceptable and reliable performance. Since Oracle's acquisition of Java, the official Java runtime environment has been distributed with corporate malware. Therefore, the Java implementation is incompatible with the spirit of open-source, and has fallen into disrepair. It can be found in an un-maintained state on Bitbucket, for the curious. There is an example port of the original Perceptron concept to WebGL under development in the examples directory of the webgpgpu project on Githib (Once this example becomes user-friendly, it will be migrated to this project). 

Since then, several implementations of video feedback have emerged. There is a small but growing community of mobile apps demonstrating the rendering technique. The rendering approach is quite efficient: at it's core it simply involves scanning over an image "display" buffer and pulling in geometrically-transformed pixels from a separate "camera" buffer, to simulate a camera pointed at a screen displaying the camera's own feed (typically with some additional geometric or color-space effects added). Small adjustements in the geometric transformations and color filters can lead to striking and beautiful effects, and it is the hope that the examples collected in this project may spur further creativity and community interest in the technique.

### License

Unless otherwise specified, media, text, and rendered outputs are licensed under the [Creative Commons Attribution Share Alike 4.0 license](https://choosealicense.com/licenses/cc-by-sa-4.0/) (CC BY-SA 4.0). Source code is licensed under the [GNU General Public License version 3.0](https://www.gnu.org/copyleft/gpl.html) (GPLv3). The CC BY-SA 4.0 is [one-way compatible](https://creativecommons.org/compatiblelicenses) with the GPLv3 license. 
This license does not apply to the project as a whole, but only to those modules or functions for which an alternative license is not otherwise specified. This project is published in the spirit (but not the letter) of the [tongue-and-cheek CRAPL license for academic code](http://matt.might.net/articles/crapl/CRAPL-LICENSE.txt). This is not a legal license as it contains internal inconsistencies (like forbidding users to exist). However, it contains a strong caution that this project has not been reviewed or prepared for use by the general public. 
