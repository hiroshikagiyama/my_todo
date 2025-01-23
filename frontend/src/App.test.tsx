import '@testing-library/jest-dom/vitest';
import { expect, test } from 'vitest'
import {render, screen} from "@testing-library/react";
import App from "./App.tsx";

test('it renders app', () => {
    render(<App />)
    screen.debug()
    expect(screen.getByText("My Todo")).toBeInTheDocument()
})