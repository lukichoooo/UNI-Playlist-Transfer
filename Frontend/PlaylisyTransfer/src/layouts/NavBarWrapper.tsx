import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar/Navbar'
import type { JSX } from 'react'

export default function NavBarWrapper(): JSX.Element
{
    return (
        <>
            <Navbar />
            <main className="container" style={{ paddingTop: '1rem', paddingBottom: '2rem' }}>
                <Outlet />
            </main>
        </>
    )
}
