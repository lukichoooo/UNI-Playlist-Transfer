export default function AboutPage()
{
    return (
        <div className="about-container">
            <h1 className="about-title">About Playlist Converter</h1>

            <div className="about-cards">
                <div className="about-card">
                    <h2 className="about-subtitle">What We Do</h2>
                    <p className="about-text">
                        Playlist Converter lets you transfer your playlists seamlessly across
                        Spotify, YouTube, SoundCloud, Apple Music, and Deezer. Fast, simple, and secure.
                    </p>
                </div>

                <div className="about-card">
                    <h2 className="about-subtitle">How OAuth2 Works</h2>
                    <p className="about-text">
                        We use <strong>OAuth2</strong>, the industry-standard authorization protocol, so
                        your login credentials are never shared with us. The streaming service issues a
                        secure token that grants access only to the parts you approve, like your playlists.
                    </p>
                </div>

                <div className="about-card">
                    <h2 className="about-subtitle">Security & Privacy</h2>
                    <p className="about-text">
                        Your privacy is our top priority. We <strong>do not store passwords</strong>. All tokens
                        are stored securely, and you can revoke access at any time from your streaming service's
                        account settings.
                    </p>
                    <p className="about-text">
                        Playlist Converter only accesses data required to perform playlist transfers. We never
                        collect extra personal information.
                    </p>
                </div>

                <div className="about-card">
                    <h2 className="about-subtitle">Transparency & Trust</h2>
                    <p className="about-text">
                        We strive for full transparency. You can view our
                        <a href="https://github.com/lukichoooo/Music-Playlist-Converter" target="_blank" rel="noopener noreferrer">
                            {" "}source code on GitHub
                        </a>, so you can see exactly how your data is handled.
                    </p>
                </div>
            </div>
        </div>
    );
}
