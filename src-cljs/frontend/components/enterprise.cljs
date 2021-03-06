(ns frontend.components.enterprise
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [clojure.string :as str]
            [frontend.async :refer [raise!]]
            [frontend.components.common :as common]
            [frontend.components.contact-form :as contact-form]
            [frontend.components.plans :as plans-component]
            [frontend.components.shared :as shared]
            [frontend.state :as state]
            [frontend.stefon :as stefon]
            [frontend.utils :as utils :include-macros true]
            [frontend.utils.ajax :as ajax]
            [goog.string :as gstr]
            [om.core :as om :include-macros true])
  (:require-macros [frontend.utils :refer [defrender html inspect]]
                   [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

(defn modal [app owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div#enterpriseModal.fade.hide.modal
        [:div.modal-body
         [:h4
          "Contact us to learn more about enterprise Continuous Delivery"]
         [:hr]
         (om/build shared/contact-form app {:opts {:enterprise? true}})]]))))

(defn arrow [name]
  [:img.arrow {:class name
               :src (utils/cdn-path (str "/img/outer/enterprise/arrow-" name ".svg"))}])

(defn language [name]
  [:img.background.language {:class name
                             :src (utils/cdn-path (str "/img/outer/languages/language-" name ".svg"))}])


(def contact-form
  (contact-form/contact-form-builder
    {:id "contact-form"
     :class "form-horizontal"
     :action "/about/contact"}
    {:params-filter (fn [{:strs [name email company phone developer-count]}]
                      {:name name
                       :email email
                       :message (gstr/format "Company: %s\nPhone: %s\nDeveloper count: %s" company phone developer-count)
                       :enterprise true})}
    (fn [control notice form-state]
      (list
        [:div.row.contact-form
         [:div.col-sm-8.col-sm-offset-2
          [:div.row
           [:div.col-sm-6
            (control :input.input-lg
                     {:type "text"
                      :name "company"
                      :required true
                      :disabled (= :loading form-state)
                      :placeholder "Company"})]
           [:div.col-sm-6
            (control :input.input-lg
                     {:type "text"
                      :name "phone"
                      :disabled (= :loading form-state)
                      :placeholder "Phone"})]]
          [:div.row
           [:div.col-sm-6
            (control :input.input-lg
                     {:type "text"
                      :name "name"
                      :required true
                      :disabled (= :loading form-state)
                      :placeholder "Name"})]
           [:div.col-sm-6
            (control :input.input-lg
                     {:type "text"
                      :name "developer-count"
                      :disabled (= :loading form-state)
                      :placeholder "# of Developers"})]]
          [:div.row
           [:div.col-sm-6
            (control :input.input-lg
                     {:type "email"
                      :name "email"
                      :require true
                      :disabled (= :loading form-state)
                      :placeholder "Email"})]
           [:div.col-sm-6
            [:div.telephone-info
             "Or call "
             [:a.telephone-number {:href "tel:+14158515247"} "415.851.5247"]
             " for an Enterprise quote."]]]]]
        (om/build contact-form/transitionable-height
                  {:class "notice"
                   :children (html
                               (when notice
                                 [:div {:class (:type notice)}
                                  (:message notice)]))})
        [:div.row
         [:div.col-xs-12.text-center
          (om/build contact-form/morphing-button {:text "Get More Info" :form-state form-state})
          [:div.success-message
           {:class (when (= :success form-state) "success")}
           "Thank you for submitting your information."
           [:br]
           "Someone from our Enterprise team will contact you within one business day."]]]))))

(defn enterprise [app owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div#enterprise
        [:div.jumbotron
         (map arrow ["left-a-1"
                     "left-a-2"
                     "left-a-3"
                     "left-a-4"
                     "right-a-1"
                     "right-a-2"
                     "right-a-3"
                     "right-a-4"])
         ;; [:img.arrow {:src (utils/cdn-path "/img/outer/enterprise/arrow-left-a-1.svg")}]
         [:section.container
          [:div.row
           [:article.hero-title.center-block
            [:div.text-center
             [:img.hero-logo {:src (utils/cdn-path "/img/outer/enterprise/logo-circleci.svg")}]]
            [:h1.text-center "Ship code at the speed of business."]
            [:h3.text-center "The same Continuous Integration and Deployment platform that developers love, with added security for the enterprise. CircleCI Enterprise lets you quickly and securely build, test, and deploy your applications."]]]

          [:div.row.text-center
           [:a.btn.btn-cta {:href "#contact-form"} "Get More Info"]]]]
        ;; need this wrapper for border-top to span the full screen
        [:div.outer-section
         [:div.container
          [:section.row
           [:div.col-xs-4
            [:article
             (common/feature-icon "circle")
             [:h2.text-center "Ship Faster"]
             [:p "The same Continuous Integration and Deployment platform that developers love, with added security for the enterprise. CircleCI Enterprise lets you quickly and securely build, test, and deploy your applications."]]
            ]
           [:div.col-xs-4
            [:article
             (common/feature-icon "security")
             [:h2.text-center "World Class Security"]
             [:p
              "You can run CircleCI Enterprise in your own private cloud or in ours, allowing you to maintain scalability while achieving enterprise level security."]]]
           [:div.col-xs-4
            [:article
             (common/feature-icon "time")
             [:h2.text-center "Focus on What Matters"]
             [:p
              "Time is a valuable resource, so you should focus on what moves the needle for your business. CircleCI removes the pain of managing build machines and scaling your build fleet, allowing developers to focus on what matters."]]]]]]
         [:div.outer-section
          [:div.container
           [:section.row
            [:div.col-xs-8.col-xs-offset-2.enterprise-integrations
             [:h2.text-center "Integrations"]
             [:p "CircleCI is built for unlimited flexibility. From hosting options to test frameworks or programming frameworks, we let you use the technology you need. GitHub Enterprise, Docker, SauceLabs, and many more."]]]
           [:section.row
            [:div.col-xs-4
             [:div.integration-logo.github
              [:img {:src (utils/cdn-path "/img/outer/enterprise/integration-github-1.svg")}]]
             [:div.integration-text
              "Enjoy all of the benefits of CircleCI's rich GitHub integration with your own GitHub Enterprise instance. Authenticate against GitHub Enterprise, see the status of CircleCI builds from your PR pages on GitHub Enterprise, and easily navigate to specific GitHub PRs and commits from CircleCI build pages."
              ;; TODO: add integrations page for GitHub
              ; [:a.integration-learn-more {:href "/integrations"} "Learn more"]
              ]]
            [:div.col-xs-4
             [:div.integration-logo.docker
              [:img {:src (utils/cdn-path "/img/outer/enterprise/integration-docker-1.svg")}]]
             [:div.integration-text
              "CircleCI Enterprise supports all of the container-oriented features Docker has to offer. Pull down base images from any registry, build your own Dockerfiles right in CircleCI, link containers together and run integration tests, and push built Docker images to production - all driven by CircleCI's simple yaml-based configuration and intuitive UI."
              [:a.integration-learn-more {:href "/integrations/docker"} "Learn more"]]]
            [:div.col-xs-4
             [:div.integration-logo.sauce-labs
              [:img {:src (utils/cdn-path "/img/outer/enterprise/integration-sauce-1.png")}]]
             [:div.integration-text
              "Test against any version of any browser with CircleCI's Enterprise SauceLabs integration. Using the Sauce Connect tunnel, you can even test applications running securely within CircleCI build containers behind your firewall. Automate your cross-browser and mobile testing."
              ;; TODO: add integrations page for Sauce Labs
              [:a.integration-learn-more {:href "/integrations/saucelabs"} "Learn more"]]]]]]
        [:section.outer-section.outer-section-condensed
          (map language ["rails-1"
                         "clojure-1"
                         "java-1"
                         "php-1"])
         [:div.container
          [:div.row
           [:div.col-xs-8.col-xs-offset-2
            [:div.testimonial
             [:img.customer-header {:src (utils/cdn-path "/img/outer/customers/customer-shopify.svg")
                                    :alt "Shopify"}]
             [:div.customer-quote {:class "quote-shopify"}
              [:blockquote "CircleCI lets us be more agile and ship product faster. We can focus on delivering value to our customers, not maintaining CI Infrastructure."]]
             [:div.customer-citation
              [:div.customer-employee-name "John Duff"]
              [:div
               "Director of Engineering at "
               [:span.customer-company-name "Shopify"]
               " – "
               [:a.customer-story {:href "/stories/shopify"}
                "Read the story"]]]]]]]]

        [:div.outer-section
         [:section.container
          (common/feature-icon "phone")
          [:h2.text-center "Learn More About CircleCI Enterprise"]
          [:div.enterprise-cta-contact
           (om/build contact-form nil)]]]]))))
