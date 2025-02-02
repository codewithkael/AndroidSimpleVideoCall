# Simple Video Call Application Using WebRTC (Android-Jetpack Compose) – Complete Guide  

This project demonstrates how to build a simple video call application using the **WebRTC protocol**. The system consists of three main components:  

1. **Signaling Server** (Backend) – Facilitates communication between peers.  
2. **Android Client** – A mobile application built with **Kotlin** and **Jetpack Compose**.  
3. **Web Client** – A browser-based version built with **React.js (Vite)**.  
4. **iOS Client** – (Placeholder for upcoming iOS development).  

---
## 🎥 Video Tutorial & Playlist  

📺 **Playlist for this Project:** [CodeWithKael Playlist](https://youtube.com/playlist?list=PLFelST8t9nqgo5vQXQOl4xmySNO0MFLas&si=TeQN8pLnanqsPd1-)

---
## 🖥 Backend (Signaling Server)  
The signaling server is implemented using **Node.js, WebSocket, and JavaScript**. It manages the initial peer connection by maintaining a simple username-to-WebSocket reference mapping. This allows:  

✅ **User Discovery** – Finding the recipient user for signaling.  
✅ **Message Forwarding** – Routing offer, answer, and ICE candidate signals between peers.  

🔗 **Source Code:** [Backend Repository](https://github.com/codewithkael/SimpleVideoCallBackend)  

---

## 📱 Android Client  
The Android version is developed using **Kotlin**, **Jetpack Compose**, and the **WebRTC library**. It integrates a **WebSocket client** to interact with the signaling server and manages:  

✅ **User Authentication (Basic Username Entry)**  
✅ **Establishing Peer Connections**  
✅ **Handling WebRTC Offer/Answer Negotiation**  
✅ **Rendering Video & Audio Streams**  

🔗 **Source Code:** [Android Repository](https://github.com/codewithkael/AndroidSimpleVideoCall)  

---

## 🌐 Web Client  
The web-based implementation is built with **React.js**, using **JSX** and **Vite** for fast performance. This version includes:  

✅ **User Input for Signaling**  
✅ **WebRTC Peer Connection Setup**  
✅ **WebSocket Integration for Communication**  
✅ **Real-time Video & Audio Streaming**  

🔗 **Source Code:** [React Repository](https://github.com/codewithkael/SimpleVideoCallReacJs)  

---

## 🍏 iOS Client *(Coming Soon!)*  
An iOS version of this project is in development by our team and will be available soon!  

---

## 📌 How It Works  
1️⃣ User has a **random 5 letters** username.  
2️⃣ The **signaling server** routes WebRTC messages between peers.  
3️⃣ Once signaling is complete, **WebRTC handles direct peer-to-peer streaming**.  
4️⃣ The application supports **audio and video calls** between users.  

---

## 🎥 Video Tutorial & Playlist  

📺 **Playlist for this Project:** [CodeWithKael Playlist](https://www.youtube.com/@codewithkael)

---

## 🎬 About My YouTube Channel – @CodeWithKael  
I create **programming tutorials**, **real-world projects**, and **tech-related content** to help developers learn and build amazing applications. From WebRTC and React to Android and Backend development, my goal is to simplify complex topics and make learning fun and practical.  

📢 If you found this project helpful, please **LIKE**, **SHARE**, and **SUBSCRIBE** to my channel **[@CodeWithKael](https://www.youtube.com/@codewithkael)**. It really helps support my work and allows me to keep creating more valuable content! 🚀  

---

If you find this project useful, consider giving a ⭐ on GitHub and subscribing to my YouTube channel! 🚀  
