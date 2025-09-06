import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import styles from "./AuthPage.module.css";

export default function RegisterPage()
{
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const [repeatPassword, setRepeatPassword] = useState("");

    const navigate = useNavigate();
    const location = useLocation();

    const handleFormRegister = (e: React.FormEvent) =>
    {
        e.preventDefault();
        setError("");

        if (password !== repeatPassword)
        {
            setError("Passwords do not match.");
            return;
        }

        // TODO: call backend with email/password to get JWT
    };


    const handleGoogleRegister = () =>
    {
        // TODO: redirect to Google OAuth2 Register
    };

    const handleGithubRegister = () =>
    {
        // TODO: redirect to GitHub OAuth2 Register
    };

    return (
        <>
            <button className={styles.homeButton + " " + styles.button} onClick={() => navigate("/")}>Home</button>

            <div className={styles.container}>
                <h1>Register</h1>
                {error && <p className={styles.error}>{error}</p>}

                <p>Create a new account</p>

                <form onSubmit={handleFormRegister} className={styles.form}>
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
                    <button onClick={handleGoogleRegister}>Register with Google</button>
                    <button onClick={handleGithubRegister}>Register with GitHub</button>
                </div>
            </div>
        </>
    );
}
