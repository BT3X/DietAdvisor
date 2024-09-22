# Diet Advisor: An Image-Based Food Intake Analysis and Meal Recommendation System

## Overview
Diet Advisor is a mobile application designed to help users monitor and improve their dietary habits by analyzing food images. The system estimates the nutritional content of meals and provides personalized dietary recommendations based on the user's health goals. This project utilizes cutting-edge machine learning, computer vision, and deep learning technologies to identify food items, estimate nutritional values, and suggest meals.

## Features
- **Food Recognition**: Detects and identifies food items from images using the YOLOv5 object detection model.
- **Nutrient Estimation**: Estimates the nutritional content of meals, including calorie and macronutrient information, through advanced image segmentation and depth estimation techniques.
- **Personalized Recommendations**: Suggests meals based on user input, such as dietary preferences and health goals.
- **User-Friendly Interface**: Simplifies the process of tracking food intake and analyzing nutritional data with an easy-to-use interface.

## System Architecture
The system is divided into the following key modules:
1. **Food Identification & Segmentation**: Uses YOLOv5 and the Segment Anything Model (SAM) to identify and isolate food items within images.
2. **Depth Estimation**: Employs the Depth Anything Model to calculate the volume of food items based on RGB images.
3. **Nutritional Content Estimation**: Calculates the mass and nutritional content of the food using Support Vector Regressor (SVR) models.
4. **Meal Recommender System**: Provides personalized meal suggestions using a large language model based on the user's health and dietary preferences.

## Dataset
The system uses a custom dataset for training and testing. The dataset includes:
- Food images with a 10 NTD coin for scale.
- Ground truth mass values for each food item.
- Segmented food items to improve accuracy in analysis.

## Technologies Used
- **YOLOv5** for food identification.
- **Segment Anything Model (SAM)** for food segmentation.
- **Depth Anything Model** for depth estimation from RGB images.
- **Flask** for backend services.
- **Support Vector Regressor (SVR)** for mass estimation.
- **GPT-4** (LLM) for generating personalized meal recommendations.

## Installation
1. Clone the repository:
    ```bash
    # TODO...
    ```

2. Install dependencies:
    ```bash
    # TODO...
    ```

3. Run the application:
    ```bash
    # TODO...
    ```

## Usage
1. Register an account and provide basic personal information (e.g., weight goals).
2. Upload an image of your meal.
3. View the estimated nutritional information.
4. Track your food intake and receive personalized meal recommendations based on your dietary preferences.

## Future Improvements
- Expand the dataset to include a wider variety of food types.
- Support multiple food containers beyond the single type of plate used.
- Develop a dedicated smartphone application to streamline food image uploads.

## License
This project is licensed under the MIT License.
