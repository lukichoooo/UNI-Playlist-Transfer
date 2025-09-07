import { Link } from "react-router-dom";
import styles from "./HomePage.module.css"
import ServiceSelectorMenu from "../../components/serviceSelectorMenu/ServiceSelectorMenu";

export default function HomePage()
{
    return (
        <div>
            <h1>Home Page</h1>
            <br />
            <h2>// TODO: fetch some data from backend to check connection</h2>
            <Link to={"/about"}>
                <button className={styles.aboutButton}>About</button>
            </Link>
            <hr />

            <ServiceSelectorMenu authenticatedServices={[]} />

        </div>
    );
}