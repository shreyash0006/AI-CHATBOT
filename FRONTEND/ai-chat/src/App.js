
import { useState, useRef, useEffect } from "react";
import "./App.css";

// Your Spring Boot backend endpoint
const API_URL = "http://localhost:8080/ai/chat";

function App() {
  const [chats, setChats] = useState([
    {
      id: Date.now(),
      title: "New conversation",
      messages: [],
    },
  ]);

  const [activeChatId, setActiveChatId] = useState(
    chats[0].id
  );
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const bottomRef = useRef(null);
  const textareaRef = useRef(null);

  const activeChat = chats.find(
    (chat) => chat.id === activeChatId
  );

  const messages = activeChat?.messages ?? [];

  useEffect(() => {
    bottomRef.current?.scrollIntoView({
      behavior: "smooth",
    });
  }, [messages, loading]);

  function updateMessages(chatId, newMessages) {
    setChats((previous) =>
      previous.map((chat) =>
        chat.id === chatId
          ? {
              ...chat,
              messages: newMessages,
              title:
                chat.title === "New conversation" &&
                newMessages.length > 0
                  ? newMessages[0].content.slice(0, 30)
                  : chat.title,
            }
          : chat
      )
    );
  }

  function createNewChat() {
    if (loading) return;

    const newChat = {
      id: Date.now(),
      title: "New conversation",
      messages: [],
    };

    setChats((previous) => [newChat, ...previous]);
    setActiveChatId(newChat.id);
    setInput("");
    setError("");
  }

  async function sendMessage(event) {
    event?.preventDefault();

    const question = input.trim();

    if (!question || loading) return;

    const chatId = activeChatId;
    const previousMessages = [...messages];

    const userMessage = {
      role: "user",
      content: question,
    };

    updateMessages(chatId, [
      ...previousMessages,
      userMessage,
    ]);

    setInput("");
    setError("");
    setLoading(true);

    try {
      // Send the user's message to Spring Boot
      const response = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          message: question,
        }),
      });

      if (!response.ok) {
        throw new Error(
          `Request failed: HTTP ${response.status}`
        );
      }

      // Support either a JSON response or plain text
      const rawResponse = await response.text();

      let data;

      try {
        data = JSON.parse(rawResponse);
      } catch {
        data = rawResponse;
      }

      // Extract the AI answer
      const answer =
        typeof data === "string"
          ? data
          : data.response ??
            data.answer ??
            data.message ??
            data.content;

      if (typeof answer !== "string") {
        throw new Error(
          "The backend response format is not recognized."
        );
      }

      // Display the AI response in the chat
      updateMessages(chatId, [
        ...previousMessages,
        userMessage,
        {
          role: "assistant",
          content: answer,
        },
      ]);
    } catch (err) {
      console.error("Chat API error:", err);

      setError(
        err.message ||
          "Unable to connect to the Spring Boot server."
      );

      updateMessages(chatId, [
        ...previousMessages,
        userMessage,
      ]);
    } finally {
      setLoading(false);
      textareaRef.current?.focus();
    }
  }

  function handleKeyDown(event) {
    if (
      event.key === "Enter" &&
      !event.shiftKey &&
      !event.nativeEvent.isComposing
    ) {
      event.preventDefault();
      sendMessage();
    }
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">✦</div>
          <span>Nova AI</span>
        </div>

        <button
          className="new-chat"
          onClick={createNewChat}
          disabled={loading}
        >
          <span>＋</span> New chat
        </button>

        <div className="sidebar-label">YOUR CHATS</div>

        <div className="chat-list">
          {chats.map((chat) => (
            <button
              key={chat.id}
              className={`chat-item ${
                chat.id === activeChatId ? "selected" : ""
              }`}
              onClick={() => {
                if (!loading) {
                  setActiveChatId(chat.id);
                  setError("");
                }
              }}
            >
              <span>◷</span>
              <span className="chat-title">
                {chat.title}
              </span>
            </button>
          ))}
        </div>

        <div className="sidebar-footer">
          <div className="avatar">AI</div>
          <div>
            <strong>AI Assistant</strong>
            <p>Powered by your backend</p>
          </div>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div className="mobile-brand">
            <span className="brand-icon">✦</span>
            Nova AI
          </div>

          <div className="model-status">
            <span className="status-dot" />
            AI Assistant
          </div>
        </header>

        <section className="conversation">
          {messages.length === 0 ? (
            <div className="welcome">
              <div className="welcome-icon">✦</div>

              <h1>What can I help you with?</h1>

              <p>
                Ask me anything. Let's explore ideas,
                solve problems, and learn something new.
              </p>

              <div className="suggestions">
                {[
                  {
                    icon: "✧",
                    title: "Explain a concept",
                    text: "Explain a complex topic simply",
                  },
                  {
                    icon: "⌘",
                    title: "Write some code",
                    text: "Help me write and debug code",
                  },
                  {
                    icon: "◈",
                    title: "Generate ideas",
                    text: "Give me creative project ideas",
                  },
                  {
                    icon: "↗",
                    title: "Learn something",
                    text: "Create a learning roadmap",
                  },
                ].map((item) => (
                  <button
                    className="suggestion"
                    key={item.title}
                    onClick={() => setInput(item.text)}
                  >
                    <span className="suggestion-icon">
                      {item.icon}
                    </span>

                    <span>
                      <strong>{item.title}</strong>
                      <small>{item.text}</small>
                    </span>
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <div className="messages">
              {messages.map((message, index) => (
                <div
                  key={index}
                  className={`message ${
                    message.role === "user"
                      ? "user-message"
                      : "assistant-message"
                  }`}
                >
                  {message.role === "assistant" && (
                    <div className="message-avatar">✦</div>
                  )}

                  <div className="message-body">
                    <div className="message-role">
                      {message.role === "user"
                        ? "You"
                        : "Nova AI"}
                    </div>

                    <div className="message-content">
                      {message.content}
                    </div>

                    {message.role === "assistant" && (
                      <button
                        className="copy-button"
                        title="Copy response"
                        onClick={() =>
                          navigator.clipboard.writeText(
                            message.content
                          )
                        }
                      >
                        Copy response
                      </button>
                    )}
                  </div>
                </div>
              ))}

              {loading && (
                <div className="message assistant-message">
                  <div className="message-avatar">✦</div>

                  <div className="message-body">
                    <div className="message-role">
                      Nova AI
                    </div>

                    <div
                      className="typing"
                      aria-label="AI is thinking"
                    >
                      <span />
                      <span />
                      <span />
                    </div>
                  </div>
                </div>
              )}

              <div ref={bottomRef} />
            </div>
          )}
        </section>

        <footer className="composer-area">
          {error && (
            <div className="error-message" role="alert">
              {error}
            </div>
          )}

          <form
            className="composer"
            onSubmit={sendMessage}
          >
            <textarea
              ref={textareaRef}
              value={input}
              onChange={(event) =>
                setInput(event.target.value)
              }
              onKeyDown={handleKeyDown}
              placeholder="Message Nova AI..."
              rows={1}
              disabled={loading}
              aria-label="Message the AI assistant"
            />

            <button
              type="submit"
              className="send-button"
              disabled={!input.trim() || loading}
              aria-label="Send message"
              title="Send message"
            >
              ↑
            </button>
          </form>

          <p className="disclaimer">
            AI can make mistakes. Verify important information.
          </p>
        </footer>
      </main>
    </div>
  );
}

export default App;
