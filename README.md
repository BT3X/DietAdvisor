# **Diet Advisor: An Image-Based Food Intake Analysis and Meal Recommendation System**

## **Overview**

Diet Advisor is an **Android application** designed to help users monitor and improve their dietary habits by analyzing food images. The app estimates the nutritional content of meals and provides personalized dietary recommendations based on users’ health goals. By leveraging advanced machine learning, computer vision, and deep learning techniques, Diet Advisor simplifies food intake tracking and meal planning, empowering users to make healthier choices.

---

## **Features**

- **Food Recognition**: Detects and identifies food items from images using the YOLOv5 object detection model.
- **Nutrient Estimation**: Calculates calorie and macronutrient information through advanced image segmentation and depth estimation techniques.
- **Personalized Recommendations**: Provides meal suggestions tailored to users' dietary preferences and health goals, powered by a large language model (GPT-4).
- **User-Friendly Interface**: Designed with a responsive and intuitive layout to enhance the user experience, making dietary tracking easy and accessible.

---

## **System Architecture**

The system consists of the following key components:

1. **Food Identification & Segmentation**:
   - Detects food items using YOLOv5.
   - Segments food items with the Segment Anything Model (SAM) for precise isolation.
2. **Depth Estimation**:
   - Uses the Depth Anything Model to determine the volume of food items from RGB images.
3. **Nutritional Content Estimation**:
   - Employs a Support Vector Regressor (SVR) to estimate food mass and calculates nutritional content.
4. **Meal Recommender System**:
   - Utilizes GPT-4 to provide personalized meal recommendations based on user profiles, dietary preferences, and health goals.

---

## **Dataset**

The project uses a custom dataset for training and testing:

- **Food Images**: Each image includes a 10 NTD coin for scale.
- **Ground Truth Mass Values**: Accurate measurements of food mass for model training.
- **Segmented Food Items**: Enhanced segmentation for better analysis and model accuracy.

---

## **Technologies Used**

- **YOLOv5** for food identification.
- **Segment Anything Model (SAM)** for food segmentation.
- **Depth Anything Model** for depth estimation.
- **Support Vector Regressor (SVR)** for mass estimation.
- **GPT-4** for generating personalized meal recommendations.
- **Android Studio and Kotlin** for mobile application development.
- **Flask** for backend services.

---

## **Video Demo**

Watch Diet Advisor in action:

- **[YouTube Demo](https://youtu.be/qvs0chYFkQ8)**

This demo provides an overview of the application’s interface, food recognition, nutritional estimation, and personalized recommendations.

---

## **Installation**

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/BT3X/DietAdvisor.git
   ```

2. **Install the Android Application**:
   - Open the project in Android Studio.
   - Build and deploy the app to an emulator or physical device.

---

## **Usage**

1. **Register and Provide Information**:
   - Create an account and enter basic personal details (e.g., age, weight, dietary goals).
2. **Upload a Meal Image**:
   - Take or upload a picture of your meal.
3. **View Results**:
   - See estimated nutritional information, including calorie and macronutrient breakdowns.
4. **Get Recommendations**:
   - Receive personalized meal suggestions tailored to your dietary preferences and goals.

---

## **Future Improvements**

- **Dataset Expansion**:
  - Include a broader range of food types and cuisines for enhanced recognition accuracy.
- **Enhanced Container Support**:
  - Add support for analyzing meals in multiple types of containers beyond plates.
- **Dedicated Mobile Enhancements**:
  - Expand functionality for seamless integration with smartphone cameras and user interfaces.

---

## **Acknowledgments**

We would like to thank the following contributors and tools that made this project possible:

- **YOLOv5** by Ultralytics for food detection and object identification.
- **Segment Anything Model (SAM)** by Meta for precise food segmentation.
- **Depth Anything Model** for advanced depth estimation from RGB images.
- **GPT-4** by OpenAI for personalized meal recommendation generation.
- **Android Studio and Kotlin** for mobile application development tools.
- **Flask** for backend server implementation.
- Team Members: Ken Chang, Kenrick Albert, Nguyen Minh Trang.

---

## **License**

This project is licensed under the MIT License.
