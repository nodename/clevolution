(ns clevolution.image_ops.nullary.gradient
	(:import (java.awt.image BufferedImage)
          (java.awt Color GradientPaint))
 (:use clojure.contrib.math))


(defn create-gradient-image
	"Create a gradient image. Arity: 0 Parameters: none"
	[gradientPaint width height]
	(let [ bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
	       g (.createGraphics bi)]
	(.setPaint g gradientPaint)
	(.fillRect g 0 0 width height)
	bi))

(defn X
	"Create a horizontal gradient image. Arity: 0 Parameters: none"
	[width height]
	(create-gradient-image (GradientPaint. 0 0 Color/black (dec width) 0 Color/white) width height))

(defn Y
	"Create a vertical gradient image. Arity: 0 Parameters: none"
	[width height]
	(create-gradient-image (GradientPaint. 0 0 Color/black 0 (dec height) Color/white) width height))
