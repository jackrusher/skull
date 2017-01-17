(ns skull.core)

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
)

(def scene (js/THREE.Scene.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; lights

(def light1 (THREE.DirectionalLight. 0xDDDDDD 1.0))
(.set (.-position light1) 30 45 -60)
(set! (.-position (.-target light1)) 60)
(.add scene light1)

(def light2 (THREE.PointLight. 0xFFFFFF 0.4 ))
(.set (.-position light2) -60 25 -40)
(.add scene light2)

(def light3 (THREE.SpotLight. 0xFFFFFF 0.5))
(.set (.-position light3) 60 -75 -40)
(set! (.-penumbra light3) 0.8)
(set! (.-angle light3) 0.349)
(.add scene light3)

(def light4 (THREE.AmbientLight. 0xFFFFFF 2.0 ))
(.add scene light4)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; camera

(def camera
  (js/THREE.PerspectiveCamera. 45 (/ (.-innerWidth js/window)
                                     (.-innerHeight js/window)) 1 2000))
(set! (.-x (.-position camera)) -10)
(set! (.-y (.-position camera)) -10)
(set! (.-z (.-position camera)) -20)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; pull in the shape, fix up some formatting problems, apply a material

(def material (js/THREE.MeshStandardMaterial. (js-obj "color"        0xD851E9,
                                                      "depthTest"    true,
                                                      "depthWrite"   true,
                                                      "emissive"     0x0,
                                                      "roughness"    0.5,
                                                      "metalness"    0.2,
                                                      "side"         js/THREE.FrontSide,
                                                      "shading"      js/THREE.SmoothShading,
                                                      "vertexColors" js/THREE.NoColors)))

(defn fix-normals
  "Fix the STL skull's banjaxed normals. Could have done this in MeshLab or something."
  [object]
  (let [geometry (js/THREE.Geometry.)]
    (doseq [[x y z] (partition 3 (array-seq (.-array (.getAttribute object "position"))))]
      (.push (.-vertices geometry) (js/THREE.Vector3. x y z)))
    (doseq [[a b c] (partition 3 (range (.-length (.-vertices geometry))))]
      (.push (.-faces geometry) (js/THREE.Face3. a b c)))
    (.mergeVertices geometry)
    (.computeVertexNormals geometry)
    geometry))

(.load (js/THREE.STLLoader.) "models/SkullTest.stl"
       (fn [obj]
         (.center obj)
         (let [mesh (js/THREE.Mesh. (fix-normals obj) material)]
           (.set (.-position mesh) 0 0 0)
           (.set (.-rotation mesh) -1.8 -0.2 -2.5)
           (.add scene mesh))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; action

(defonce renderer
  (let [rndr (THREE.WebGLRenderer.)]
    (.setPixelRatio rndr (.-devicePixelRatio js/window))
    (.setSize rndr (.-innerWidth js/window) (.-innerHeight js/window))
    (.setClearColor rndr 0xCCFFCC)
    (.appendChild (.-body js/document) (.-domElement rndr))
    rndr))

(def control (THREE.OrbitControls. camera))

(defn render []
  (.requestAnimationFrame js/window render)
  (.update control)
  (.render renderer scene camera))

(render)
