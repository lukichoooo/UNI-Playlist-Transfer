import ServiceSelectorMenu from "../../components/serviceSelectorMenu/ServiceSelectorMenu";
import styles from "./HomePage.module.css";

export default function HomePage()
{
    return (
        <div className={styles["page-container"]}>
            <h1>Home Page</h1>
            <hr />
            <ServiceSelectorMenu />
        </div>
    );
}
