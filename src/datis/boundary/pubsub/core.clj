(ns datis.boundary.pubsub.core)

(defprotocol Publisher
  (publish! [this arg-map]))
