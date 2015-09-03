# clevolution

<p>Please see the <a href="http://nodename.github.io/clevolution/">project page</a> (a bit outdated).</p>


Copyright (C) 2012-2015 Alan Shaw

Distributed under the Eclipse Public License, the same as Clojure.

## GUI

% lein run



## API

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
  
  ;; show the generated image in a JFrame:
  
  (show-clisk-image generator-string)
  
  ;; show the image file in a JFrame:
  
  (show-clisk-file uri & more) 
