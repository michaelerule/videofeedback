
perceptron_maps = ["z",
"z z / 2",
"z z z / 6",
"z abs(z) / 2",
"e^z+e^(i z)",
"e^z+e^(-i z)",
"e^z+e^(z e^(i pi/4))",
"e^z+e^(z e^(i pi/-4))",
"1/(z e^(i 2 pi/3)+1.4)+1/(z e^(i -2 pi/3)+1.4)+1/(z+1.4)",
"conj(e^z+e^(i z))",
"conj(e^z+e^(-i z))",
"conj(e^z+e^(z e^(i pi/4)))",
"conj(e^z+e^(z e^(i pi/-4)))",
"abs(z) e^(2*i arg(z)) *2",
"z z e^(i abs(z))",
"z z z e^(i abs(z))",
"z e^(i abs(z)) abs(z)",
"sin(z)^2",
"cos(z)^2",
"z z+2 log(z)/pi",
"(z+1)/(z-1)+(z-1)/(z+1)",
"(z+i)/(z-i)+(z-i)/(z+i)"];


function bind_perceptron_key_listener(perceptron_model) {
    document.onkeypress = function(e) {
        var k = (e.which) ? e.which : e.keyCode;
        apply_perceptron_key_event(k, perceptron_model);
        perceptron_model.compile_kernel();
        console.log('Key pressed: '+k);
        //console.log(perceptron_model);
    };
}

/** Takes a key code and a perceptron state model, and modified the state model
  */
function apply_perceptron_key_event(k, P) {
    // Common key codes (not exclusive)
    // # denotes numeric keypad codes (distinct from numbers in a row
    // above QWERTY
    switch (k) {
        case 63: /*?: toggle this help menu */
             // TODO
             toggle_help();
             break;
        case 19: /*PAUSE: see SPACE*/ 
        case 32: /*SPACE: play/pause*/ 
             // Note: this bypasses the perceptron state object and
             // just uses global variables to start / stop 
             if (perceptron_running) stop_perceptron();
             else start_perceptron();
             break;
        case 44: /*,: decrease motion blur*/ 
            P.motion_blur = Math.max(0.0,P.motion_blur-0.1);
            console.log('less blur');
            break;
        case 46: /*.: increase motion blur*/ 
            P.motion_blur = Math.min(1.0,P.motion_blur+0.1);
            console.log('more blur');
            break;
        case 91: /*[: decrease noise level*/ 
            P.noise_level = Math.max(0.0,P.noise_level-0.1);
            if (P.noise_level<=0) P.do_noise=false;
            console.log('less noise');
            break;
        case 93: /*]: increase noise level*/ 
            P.noise_level = Math.min(1.0,P.noise_level+0.1);
            if (P.noise_level>0) P.do_noise=true;
            console.log('more noise');
            break;
        case 97 : /*a: cycle through aux buffer sources (what?!)*/
            P.aux_mode = (P.aux_mode+1)%3;
            break;
        case 65 : /*A: not bound*/ break;
        case 98 : /*b: cycle through boundary modes (what?!)*/ 
            P.bounds_mode = (P.bounds_mode+1)%3;
            break;
        case 66 : /*B: not bound*/ break;
        case 99 : /*c: toggle brightness / contrast control*/ 
            P.do_conbrite ^= true;
            break;
        case 67 : /*C: not bound*/ break;
        case 100: /*d: not bound*/ break;
        case 68 : /*D: not bound*/ break;
        case 101: /*e: not bound*/ 
            break;
        case 69 : /*E: not bound*/ break;
        case 102: /*f: not bound*/ break;
        case 70 : /*F: not bound*/ break;
        case 103: /*g: cycle through gradient modes*/ 
            P.gradient_mode = (P.gradient_mode+1)%3;
            P.do_gradient = P.gradient_mode!=0;
            break;
        case 71 : /*G: not bound*/ break;
        case 104: /*h: toggle hue-saturation adjustment*/
            P.do_huesat ^= true;
            break;
        case 72 : /*H: not bound*/ break;
        case 105: /*i: toggle color inversion*/ 
            P.do_invert ^= true;
            break;
        case 73 : /*I: not bound*/ break;
        case 106: /*j: not bound*/ break;
        case 74 : /*J: not bound*/ break;
        case 107: /*k: not bound*/ break;
        case 75 : /*K: not bound*/ break;
        case 108: /*l: not bound*/ 
        case 76 : /*L: increment external image texture*/ 
             // Note: this bypasses the perceptron state object
            next_image(); // will crash
            break;
        case 109: /*m: toggle motion blur*/ 
            P.do_mblur ^= true;
            break;
        case 77 : /*M: not bound*/ break;
        case 110: /*n: toggle noise*/
            P.do_noise ^= true;
            break;
        case 78 : /*N: not bound*/ break;
        case 111: /*o: not bound*/ break;
        case 79 : /*O: not bound*/ break;
        case 112: /*p: not bound*/ break;
        case 80 : /*P: not bound*/ break;
        case 113: /*q: move to the next complex map*/ 
            P.map_index = (P.map_index+1)%(perceptron_maps.length);
            P.map = perceptron_maps[P.map_index];
            break;
        case 81 : /*Q: not bound*/ break;
        case 114: /*r: cycle through reflection modes*/ 
            P.reflection_mode = (P.reflection_mode+1)%4;
            break;
        case 82 : /*R: not bound*/ break;
        case 115: /*s: not bound*/ break;
        case 83 : /*S: not bound*/ break;
        case 116: /*t: not bound*/ break;
        case 84 : /*T: not bound*/ break;
        case 117: /*u: not bound*/ break;
        case 85 : /*U: not bound*/ break;
        case 118: /*v: not bound*/ break;
        case 86 : /*V: not bound*/ break;
        case 119: /*w: move to previous complex map*/ 
            P.map_index = (P.map_index+perceptron_maps.length-1)%(perceptron_maps.length);
            P.map = perceptron_maps[P.map_index];
            break;
        case 87 : /*W: not bound*/ break;
        case 120: /*x: not bound*/ break;
        case 88 : /*X: not bound*/ break;
        case 121: /*y: not bound*/ break;
        case 89 : /*Y: not bound*/ break;
        case 122: /*z: not bound*/ break;
        case 90 : /*Z: not bound*/ break;


    }
}
