(ns stockfighter.level-1
  (:require [clj-http.client :as http]
            [environ.core :refer [env]]
            [clojure.data.json :as json]))

(def api-key (env :stockfighter-key))
(def auth-header {"X-Starfighter-Authorization" api-key})
(def gm-url "https://www.stockfighter.io/gm")
(def trade-url "https://www.stockfighter.io/ob/api")
(def level-one "/levels/first_steps")

; test data
;(def exchange "TESTEX")
;(def stock "FOOBAR")
;(def account "EXB123456")

(def state (atom {:instanceId nil
                  :venue      nil
                  :ticker     nil
                  :account    nil}))

(defn parse-body [response]
  (json/read-str (:body response) :key-fn keyword))

(defn order [direction quantity stock venue account type]
  (let [response (http/post (str trade-url "/venues/" venue "/stocks/" stock "/orders")
                            {:headers      auth-header
                             :content-type :json
                             :body         (json/write-str {:account   account
                                                            :venue     venue
                                                            :stock     stock
                                                            :qty       quantity
                                                            :direction direction
                                                            :orderType type})})
        body (parse-body response)]
    body))

(defn init-level []
  (let [response (http/post (str gm-url level-one) {:headers auth-header})
        body (parse-body response)
        instance (swap! state assoc :instanceId (body :instanceId))
        account (swap! state assoc :account (body :account))
        ticker (swap! state assoc :ticker (first (body :tickers)))
        venue (swap! state assoc :ticker (first (body :venues)))]
    (if (body :ok)
      (prn "success! ready" instance account ticker venue)
      ;(order "buy" 100 ticker venue account "market")
      (prn "error" response)
      )
    ))

;;