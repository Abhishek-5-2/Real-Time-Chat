'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var roomId = null;
var sessionId = Date.now().toString() + Math.floor(Math.random() * 1000);
var lastSenderSessionId = null;
const currentlyTyping = new Set();





var colors = [
    '#e6194b', '#3cb44b', '#ffe119', '#0082c8',
    '#f58231', '#911eb4', '#46f0f0', '#f032e6',
    '#d2f53c', '#fabebe', '#008080', '#e6beff',
    '#aa6e28', '#fffac8', '#800000', '#aaffc3',
    '#808000', '#ffd8b1', '#000080', '#808080',
    '#000000', '#ff4500', '#2e8b57', '#1e90ff'
];
document.addEventListener('DOMContentLoaded', () => {
    const emojiBtn = document.querySelector('#emoji-btn');
    const messageInput = document.querySelector('#message');
    
    if (typeof EmojiButton !== 'undefined') {
        const picker = new EmojiButton({
            position: 'bottom-start', // other options: 'top-start', 'bottom-start', 'bottom-end'
            autoHide: false,     // optional: keeps the picker open until clicked outside
            theme: 'dark'       // or 'light'
        });
        
        picker.on('emoji', emoji => {
            messageInput.value += emoji;
            messageInput.focus();
        });

        emojiBtn.addEventListener('click', () => {
            picker.togglePicker(emojiBtn);
        });
    } else {
        console.error('EmojiButton is not defined — check script loading order.');
    }
});

function connect(event) {
    event.preventDefault();

    username = document.querySelector('#name').value.trim();
    roomId = document.querySelector('#roomId').value.trim();

    if (username && roomId) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
}

function onConnected() {
    console.log("WebSocket connected!");

    // Subscriptions
    stompClient.subscribe(`/topic/messages/${roomId}`, onMessageReceived);
    stompClient.subscribe(`/topic/typing/${roomId}`, onTypingReceived);
    stompClient.subscribe(`/topic/onlineUsers/${roomId}`, onOnlineUsersReceived);
    stompClient.subscribe(`/user/queue/history`, onMessageReceived);

    // Notify server about new user
    stompClient.send(`/app/chat/${roomId}/addUser`, {}, JSON.stringify({
        sender: username,
        type: 'JOIN',
        sessionId: sessionId
    }));

    // Request WebSocket-based message history (optional)
    stompClient.send(`/app/chat/${roomId}/history`, {});

    // Load REST API history (safe now that roomId is confirmed)
    fetch(`/api/messages/${roomId}`)
        .then(response => response.json())
        .then(messages => {
            if (Array.isArray(messages)) {
                messages.forEach(onMessageReceived);
            } else {
                console.error('Expected an array but got:', messages);
            }
        })
        .catch(error => console.error('Failed to load messages:', error));

    // UI updates
    connectingElement.classList.add('hidden');
    document.querySelector('#room-name').textContent = `Chat Room: ${roomId}`;
}

function onOnlineUsersReceived(payload) {
    const users = JSON.parse(payload.body);
    const onlineUsersList = document.getElementById('online-users');
    const userCountSpan = document.getElementById('user-count');

    onlineUsersList.innerHTML = ''; // Clear old list

    users.forEach(user => {
        const li = document.createElement('li');
        li.textContent = user;
        li.style.color = getAvatarColor(user);
        onlineUsersList.appendChild(li);
    });

    // ✅ Update the count
    userCountSpan.textContent = users.length;
}



function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function onTypingReceived(payload) {
    const typingData = JSON.parse(payload.body);
    const typingIndicator = document.getElementById('typingIndicator');

    if (typingData.sessionId === sessionId) return;

    const name = typingData.sender;

    if (typingData.type === 'TYPING') {
        currentlyTyping.add(name);
    } else if (typingData.type === 'STOP_TYPING') {
        currentlyTyping.delete(name);
    }

    updateTypingIndicator();
}

function updateTypingIndicator() {
    const typingIndicator = document.getElementById('typingIndicator');
    const names = Array.from(currentlyTyping);

    if (names.length === 0) {
        typingIndicator.textContent = '';
        typingIndicator.classList.remove('typing-indicator');
        return;
    }

    const formatted =
        names.length === 1
            ? `${names[0]} is typing`
            : `${names.slice(0, -1).join(', ')} and ${names.slice(-1)} are typing`;

    typingIndicator.textContent = formatted;
    typingIndicator.classList.add('typing-indicator'); // triggers animated dots via CSS
}



function sendMessage(event) {
    event.preventDefault();

    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            roomId: roomId,
            sessionId: sessionId,
            timestamp: new Date().toISOString() // ✅ Add timestamp
        };
        console.log("🔼 Sending chat message:", chatMessage); 
        getAvatarColor(username, sessionId); // Cache color
        stompClient.send(`/app/chat/${roomId}`, {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

function onMessageReceived(payload) {
    var message;

    // Handle WebSocket message (has .body)
    if (payload && typeof payload.body === 'string') {
        try {
            message = JSON.parse(payload.body);
        } catch (e) {
            console.error('Failed to parse WebSocket message:', payload.body);
            return;
        }
    } 
    // Handle REST message (already an object)
    else if (typeof payload === 'object') {
        message = payload;
    } 
    else {
        console.error('Invalid message payload:', payload);
        return;
    }

    console.log("Message received:", message);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        var eventText = document.createTextNode(
            message.type === 'JOIN' ?`${message.sender} joined!` : `${message.sender} left!`
        );
        messageElement.appendChild(eventText);
    } else {
        messageElement.classList.add('chat-message');

        const isOwnMessage = message.sessionId === sessionId;
        messageElement.classList.add(isOwnMessage ? 'own-message' : 'other-message');

        const isSameSender = message.sessionId === lastSenderSessionId;

        if (!isSameSender) {
            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode(message.sender[0]);
            avatarElement.appendChild(avatarText);

            var color = getAvatarColor(message.sender);
            avatarElement.style['background-color'] = color;

            messageElement.appendChild(avatarElement);

            var usernameElement = document.createElement('span');
            usernameElement.appendChild(document.createTextNode(message.sender));
            messageElement.appendChild(usernameElement);
        } else {
            messageElement.classList.add('same-sender');
        }

        // Message bubble
        var bubble = document.createElement('div');
        bubble.classList.add('chat-bubble');

        const emojiRegex = /^\p{Emoji}+$/u;
        const isSingleEmoji = emojiRegex.test(message.content.trim());

        bubble.textContent = message.content;

        if (isSingleEmoji && message.content.trim().length <= 3) {
            bubble.classList.add('single-emoji');
        }
        

        // Timestamp
        const timestamp = message.timestamp ? new Date(message.timestamp) : new Date();
        const timeString = timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        var timestampElement = document.createElement('span');
        timestampElement.classList.add('timestamp');
        timestampElement.textContent = timeString;

        bubble.appendChild(timestampElement);
        messageElement.appendChild(bubble);

        lastSenderSessionId = message.sessionId;
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(sender) {
    let storedColors = JSON.parse(localStorage.getItem('avatarColors')) || {};

    if (storedColors[sender]) {
        return storedColors[sender];
    }

    let hash = 2166136261;
    for (let i = 0; i < sender.length; i++) {
        hash ^= sender.charCodeAt(i);
        hash *= 16777619;
    }
    hash = Math.abs(hash);
    let color = colors[hash % colors.length];

    storedColors[sender] = color;
    localStorage.setItem('avatarColors', JSON.stringify(storedColors));

    return color;
}


window.addEventListener('beforeunload', function () {
    if (stompClient && stompClient.connected) {
        stompClient.send(`/app/chat/${roomId}/removeUser`, {}, JSON.stringify({
            sender: username,
            type: 'LEAVE',
            sessionId: sessionId
        }));
        stompClient.disconnect();
    }
});


let typingTimeout;

messageInput.addEventListener('input', function () {
    if (stompClient && stompClient.connected) {
        stompClient.send(`/app/chat/${roomId}/typing`, {}, JSON.stringify({
            sender: username,
            type: 'TYPING',
            sessionId: sessionId
        }));
    }

    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        stompClient.send(`/app/chat/${roomId}/typing`, {}, JSON.stringify({
            sender: username,
            type: 'STOP_TYPING',
            sessionId: sessionId
        }));
    }, 1500); // stops typing after 1.5s of inactivity
});
const sidebar = document.getElementById("userSidebar");
  const backdrop = document.getElementById("sidebarBackdrop");
  const toggleBtn = document.getElementById("toggle-users-btn");

  toggleBtn.addEventListener("click", () => {
    sidebar.classList.toggle("open");
    backdrop.classList.toggle("show");
  });

  backdrop.addEventListener("click", () => {
    sidebar.classList.remove("open");
    backdrop.classList.remove("show");
  });


usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);