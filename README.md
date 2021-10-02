# **HistoQuiz**

## About the project

HistoQuiz is a android app developed in Kotlin language and aims to help in knowledge development related to histology . 

![Badge](https://img.shields.io/badge/build-passing-brightgreen)

## :pencil:Table of contents

- [About the project](#about-the-project)
- [Features](#features)
- [Development](#development)
  - [Setup](#setup)
  - [Configuration](#configuration)
  - [Building the project](#building-the-project)
- [Author](#author)

## :heavy_check_mark:Features

- [x] User sign-in and sign-up
- [x] Add and remove friends
- [x] Invite a friend and play against him
- [x] Play against your device
- [x] Review the contents about histology covered in the game
- [x] Keep track of your performance in the game
- [ ] Play the game with one pair of players against another

## :computer:Development

### :wrench:Setup

You will need to download and install in your computer:

- [Java 11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
- [Android Studio IDE](https://developer.android.com/studio)
- [Gradle 7.0.2](https://gradle.org/install/)

### :gear:Configuration

In order to properly execute the project, you will need do the following steps:

<details>
	<summary>Setup a project in Google Firebase Console</summary>
    	<ul>
        	<li>
                To do so, you may access this <a src="https://console.firebase.google.com/?hl=pt-							br">link</a>.
            </li>
        </ul>
</details>

<details>
	<summary>Configure your app in your Firebase project</summary>
    	<ul>
        	<li>
                The configuration should be done using <b>com.lenda.histoquiz</b> as package name
            </li>
            <li>
            	At the end of the setup, a <b>google-services.json</b> will provided, which you should download put in HistoQuiz <bf>app</bf> folder
            </li>
        </ul>
</details>

<details>
	<summary>Create and configure a Firebase Firestore Database</summary>
    	<ul>
        	<li>
                You should select to create database, and choose <b>init in production mode</b>
            </li>
            <li>
            	The Cloud Firestore Local can remains the same as the suggested from Firebase
            </li>
            <li>
            	After it has been created, you should go in <b>rules</b> tab and paste the following code:
              <pre class="line-numbers" style="white-space: pre-line">
<code class="language-css" style="white-space: pre-line">
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if request.auth.uid != null;
      }
    }
  }
</code>
</pre>
            </li>
        </ul>
</details>

<details>
	<summary>Create and configure a Firebase Realtime Database</summary>
    	<ul>
        	<li>
                You should select to create database and left Realtime Database Local in the same as suggested from Firebase
            </li>
            <li>
            	The init mode should be set to <b>blocked mode</b>
            </li>
            <li>
            	After it has been created, you should go in <b>rules</b> tab and paste the following code:
              <pre class="line-numbers" style="white-space: pre-line">
<code class="language-css" style="white-space: pre-line">
  {
  "rules": {
    ".read": true,
    ".write": true
  }
}
</code>
</pre>
            </li>
        </ul>
</details>

In order to load the slide images, taking into account that **laminas**, **perguntas** e **sistemas** all represents main collections, the app expects to find in **Firebase Firestore Database** the following structure:

```
├── laminas
│   └── slide
│   	├── code
│   	├── images
│   	└── systemId 
├── sistemas
│   └── system
│   	└── code 
└── perguntas
  	 └── questionCategory
   		└── questions 
```

The meaning of each one of the above terms are described in the table bellow. You should keep in mind that it's allowed to have only **one** of each collection described, each document must have at least one, and all other data types are allowed only one.

| **Item**         | Data type  | **Meaning**                                                  |
| :--------------- | ---------- | ------------------------------------------------------------ |
| laminas          | Collection | Contains all the slides that are used in the game            |
| slide            | Document   | Name of this slide                                           |
| images           | Array      | Contains the paths to all of the photos that represents this slide |
| systemId         | Number     | Id that represents the system that this slide belongs        |
| sistemas         | Collection | Contains all human systems that this game covers             |
| system           | Document   | Name of this system                                          |
| code             | Number     | Id used to represent this system                             |
| perguntas        | Collection | Contais all questions that are part of this game             |
| questionCategory | Document   | Category that the following questions belongs to             |
| questions        | Array      | Array contaning one question as a name and all its itens are answers of this question to all of the slides |

### :pencil2:Building the project

In order to execute the project, you can follow these stepes:

- [Activate debug mode in an android device](https://developer.android.com/studio/debug/dev-options)
- Connect it to your computer
- Run **SignInActivity.kt** file

> If you don't want to do this, you can also create a virtual device following [these stepes](https://developer.android.com/studio/run/managing-avds) and emulate HistoQuiz directly on your computer.

## :thought_balloon:Author

This project was developed by [Victória Gomes](https://github.com/victoriaogomes) as a undergraduate thesis :heart:.
