import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import styles from "./Navbar.module.css";
import { authService } from "../../services/authService";

export default function Navbar()
{
    const [open, setOpen] = useState(false);
    const navigate = useNavigate();
    const loggedIn = authService.isLoggedIn();

    const handleLogout = () =>
    {
        authService.removeToken();
        navigate("/login");
    };

    return (
        <header className={styles.nav}>
            <div className={styles.inner}>
                <NavLink to="/" className={styles.brand} onClick={() => setOpen(false)}>
                    PlaylistConverter
                </NavLink>

                <button
                    className={styles.toggle}
                    aria-expanded={open}
                    aria-label="Toggle navigation"
                    onClick={() => setOpen((v) => !v)}
                >
                    <span className={styles.burger} />
                </button>

                <nav className={`${styles.links} ${open ? styles.open : ""}`} aria-label="Main">
                    <NavLink to="/dashboard" className={({ isActive }) => (isActive ? styles.active : styles.link)}>
                        Dashboard
                    </NavLink>
                    <NavLink to="/profile" className={({ isActive }) => (isActive ? styles.active : styles.link)}>
                        Profile
                    </NavLink>
                    <NavLink to="/about" className={({ isActive }) => (isActive ? styles.active : styles.link)}>
                        About
                    </NavLink>

                    {!loggedIn ? (
                        <NavLink to="/login" className={({ isActive }) => (isActive ? styles.active : styles.link)}>
                            Login
                        </NavLink>
                    ) : (
                        <button className={styles.link} onClick={handleLogout}>
                            Logout
                        </button>
                    )}
                </nav>
            </div>
        </header>
    );
}
