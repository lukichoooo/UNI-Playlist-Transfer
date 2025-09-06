import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import styles from "./AuthPage.module.css";
import { authService } from "../../services/authService";


export default function LoginPage()
{
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const navigate = useNavigate();
    const location = useLocation();
    const from = (location.state as any)?.from?.pathname || "/dashboard";

    const handleFormLogin = async (e: React.FormEvent) =>
    {
        e.preventDefault();
        setError("");
        try
        {
            await authService.login(username, password); // ✅ now sends username
            navigate(from, { replace: true });
        } catch (err: any)
        {
            setError(err.response?.data?.message || "Login failed");
        }
    };

    return (
        <>
            <button
                className={styles.homeButton + " " + styles.button}
                onClick={() => navigate("/")}
            >
                Home
            </button>
            <div className={styles.container}>
                <h1>Login</h1>
                {error && <p className={styles.error}>{error}</p>}

                <p>Enter your username and password</p>

                <form onSubmit={handleFormLogin} className={styles.form}>
                    <label>Username</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
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

                <p>
                    Don't have an account?{" "}
                    <span className={styles.linkText} onClick={() => navigate("/register")}>
                        Register
                    </span>
                </p>

                <div className={styles.oauth}>
                    <button onClick={authService.googleLogin}>Login with Google</button>
                    <button onClick={authService.githubLogin}>Login with GitHub</button>
                </div>
            </div>
        </>
    );
}
