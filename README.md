# clevolution

<p>Please see the <a href="http://nodename.github.io/clevolution/">project page</a>.</p>

Clevolution started as a library inspired by Karl Sims' SIGGRAPH talk.
The library functions are still here, and accessible from the REPL,
but most recent development has been devoted to the GUI application.
If you want to experiment with either, you must clone this repository.
To start the app, you can cd into the repo directory and enter

lein run

The app has two main tabs, the Current Image tab and the Mutations tab.
Let's look at the Current Image tab first.

When the app starts up it has loaded the expression (we also call it a generator) "black".
This expression appears in the Expression Editor panel, and the expression is evaluated;
this results in an image, displayed on the left.
Unsurprisingly the image displayed by evaluating "black" is black.

Let's edit the expression by replacing the word "black" with "red."
Now we can click the Evaluate button at the bottom of the editor,
and we should see the image turn red.

Let's get a new random expression by clicking the Fresh button under Generate.
A new expression appears in the editor, and it's evaluated to produce a new image.

The expressions (generators) are randomly generated strings made up of image-processing
primitives in the language of Clisk, the CLojure Image Synthesis Kit -- a DSL and library by Mike Anderson.

Sometimes an expression generated by Clevolution cannot be evaluated.
In this case an image of exclamation points appears, and the Image Status will show as FAILED.
This occurs when the expression is invalid Clisk, usually due to a type error,
for example passing a scalar to a function that requires a vector.
I'm not attempting to prevent this but I am considering implementing automatic
do-over when it happens. (Failure to evaluate is reported more or less immediately;
actual evaluation takes longer.)

And quite often the expression will produce a boring image. Such is life.

But let's say we've got something of possible interest showing.
(If you'd like, you can use the File menu to Load a png file from the images folder of the
Clevolution project.)

Now we can click the Mutations button, and the view will switch to the Mutations tab,
where variations on the current expression are generated and evaluated.

If you like one of these images, you can right-click on it and select "Set As Current Image,"
and you'll be back in the Current Image tab, ready to explore this image or save it to a file.

Clevolution saves an image to a PNG file with the image size as specified in the "Image Size"
widget at top left of the control panel, and with the generator expression embedded in the
PNG header. When Clevolution loads a file, it actually ignores the image; it evaluates
and displays the generator found in the PNG header. (Any difference from the image in the file
is a BUG which arises from concurrency interference with setting noise seed values.
This will be fixed sometime.)

(to be continued)


## GUI Application

% lein run



## Library API

% lein repl

  (use 'clevolution.core)

  (def output-file "images/test.png")


  ;; generate a random expression:
  
  (random-clisk-string)

  ;; generate a random expression and evaluate it, saving the resulting image to a file:
  
  (save-clisk-image (random-clisk-string) output-file)

  ;; evaluate an explicit expression, saving the resulting image to a file
  
  ;; (This one is a Galois field (http://nklein.com/2012/05/visualizing-galois-fields/):
  
  (save-clisk-image "(vxor x y)" output-file)

  ;; generate 1000 random expressions, saving each with its image to a file:
  
  (def output-file-path "F:\\clisk-images\\")
  (dotimes [n 1000]
    (make-random-clisk-file output-file-path n))

  ;; read back the expression that generated the image in a file:
  
  (get-generator-string uri)
  
  ;; show the generated image in the GUI:
  
  (show-clisk-image generator-string)
  
  ;; show the image file in the GUI:
  
  (show-clisk-file uri & more) 




Copyright (C) 2012-2015 Alan Shaw

Distributed under the Eclipse Public License, the same as Clojure.