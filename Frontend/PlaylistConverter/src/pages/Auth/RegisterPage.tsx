import { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./AuthPage.module.css";
import { authService } from "../../services/authService";

export default function RegisterPage()
{
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [repeatPassword, setRepeatPassword] = useState("");
    const [error, setError] = useState("");

    const navigate = useNavigate();

    const handleFormRegister = async (e: React.FormEvent) =>
    {
        e.preventDefault();
        setError("");

        if (password !== repeatPassword)
        {
            setError("Passwords do not match.");
            return;
        }

        try
        {
            await authService.register(username, password); // ✅ sends username
            navigate("/dashboard");
        } catch (err: any)
        {
            setError(err.response?.data?.message || "Registration failed");
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
                <h1>Register</h1>
                {error && <p className={styles.error}>{error}</p>}

                <p>Create a new account</p>

                <form onSubmit={handleFormRegister} className={styles.form}>
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

                    <label>Repeat Password</label>
                    <input
                        type="password"
                        value={repeatPassword}
                        onChange={(e) => setRepeatPassword(e.target.value)}
                        required
                    />

                    <button type="submit" className={styles.button}>
                        Register
                    </button>
                </form>

                <p>
                    Already have an account?{" "}
                    <span className={styles.linkText} onClick={() => navigate("/login")}>
                        Login
                    </span>
                </p>

                <div className={styles.oauth}>
                    <button onClick={authService.googleLogin}>Register with Google</button>
                    <button onClick={authService.githubLogin}>Register with GitHub</button>
                </div>
            </div>
        </>
    );
}
