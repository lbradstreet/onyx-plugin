(ns onyx.plugin.{{medium}}-output
  (:require [onyx.peer.function :as function]
            [onyx.plugin.protocols :as p]
            [taoensso.timbre :refer [debug info] :as timbre]))

(defn inject-into-eventmap
  [event lifecycle]
  {:{{medium}}/example-datasink (atom (list))})

;; Map of lifecycle calls that are required to use this plugin.
;; Users will generally always have to include these in their lifecycle calls
;; when submitting the job.
(def writer-calls
  {:lifecycle/before-task-start inject-into-eventmap})

(defrecord ExampleOutput []
  p/Plugin
  (start [this event]
    ;; Initialize the plugin, generally by assoc'ing any initial state.
    this)

  (stop [this event]
    ;; Nothing is required here. However, most plugins have resources
    ;; (e.g. a connection) to clean up.
    ;; Mind that such cleanup is also achievable with lifecycles.
    this)

  p/Checkpointed
  (checkpoint [this]
    ;; Nothing is required here. This is normally useful for checkpointing in
    ;; input plugins.
    nil)

  (recover! [this replica-version checkpoint]
    ;; Nothing is required here. This is normally useful for checkpointing in
    ;; input plugins.
    this)

  (checkpointed! [this epoch]
    ;; Nothing is required here. This is normally useful for checkpointing in
    ;; input plugins.
    false)

  p/BarrierSynchronization
  (synced? [this epoch]
    ;; Nothing is required here. This is commonly used to check whether all
    ;; async writes have finished.
    true)

  (completed? [this]
    ;; Nothing is required here. This is commonly used to check whether all
    ;; async writes have finished (just like synced).
    true)

  p/Output
  (prepare-batch [this event replica messenger]
    ;; Nothing is required here. This is useful for some initial preparation,
    ;; before write-batch is called repeatedly.
    true)

  (write-batch [this {:keys [onyx.core/write-batch {{medium}}/example-datasink]} replica messenger]
    ;; Write the batch to your datasink.
    ;; In this case we are conjoining elements onto a collection.
    (loop [batch write-batch]
      (if-let [msg (first batch)]
        (do
          (swap! example-datasink conj msg)
          (recur (rest batch)))))
    true))

;; Builder function for your output plugin.
;; Instantiates a record.
;; It is highly recommended you inject and pre-calculate frequently used data 
;; from your task-map here, in order to improve the performance of your plugin
;; Extending the function below is likely good for most use cases.
(defn output [pipeline-data]
  (->ExampleOutput))
