import { Link } from "react-router-dom";

export default function HomePage()
{
    return (
        <div>
            <h1>Home Page</h1>
            // TODO: fetch some data from backend to check connection

            <hr />

            <Link to={"/about"}>
                <button>About</button>
            </Link>
        </div>
    );
}