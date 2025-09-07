import { Link } from "react-router-dom";
import "./Footer.css";

export default function Footer()
{
    return (
        <footer className="footer">
            <p className="footer-text">
                © {new Date().getFullYear()} Playlist Converter
            </p>
            <nav className="footer-links">
                <Link to="/about" className="footer-link">
                    About
                </Link>
                <a
                    href="https://github.com/lukichoooo/Music-Playlist-Converter"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="footer-link"
                >
                    GitHub
                </a>
            </nav>
        </footer>
    );
}
