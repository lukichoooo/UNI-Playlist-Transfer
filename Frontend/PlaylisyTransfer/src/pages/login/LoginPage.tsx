import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import styles from "./LoginPage.module.css";

export default function LoginPage()
{
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const navigate = useNavigate();
    const location = useLocation();
    const from = (location.state as any)?.from?.pathname || "/dashboard";

    const handleLogin = (e: React.FormEvent) =>
    {
        e.preventDefault();
        setError("");
        // TODO: call backend with email/password to get JWT
    };

    const handleGoogleLogin = () =>
    {
        // TODO: redirect to Google OAuth2 login
    };

    const handleGithubLogin = () =>
    {
        // TODO: redirect to GitHub OAuth2 login
    };

    return (
        <div className={styles.container}>
            <h1>Login</h1>
            {error && <p className={styles.error}>{error}</p>}

            <form onSubmit={handleLogin} className={styles.form}>
                <label>Email</label>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label>Password</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <button type="submit" className={styles.button}>
                    Login
                </button>
            </form>

            <div className={styles.oauth}>
                <button onClick={handleGoogleLogin}>Login with Google</button>
                <button onClick={handleGithubLogin}>Login with GitHub</button>
            </div>
        </div>
    );
}
