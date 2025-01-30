import '@testing-library/jest-dom/vitest';
import {describe, expect, test, vitest, afterEach} from 'vitest'
import {cleanup, render, screen, waitFor, within} from "@testing-library/react";
import App from "./App.tsx";
import {userEvent} from "@testing-library/user-event";

const spyFetchGetJson = vitest.hoisted(() => vitest.fn())
const mockFetch = vitest.fn().mockResolvedValue({json: spyFetchGetJson})
vitest.stubGlobal("fetch", mockFetch)

describe("App Tests", () => {

    afterEach(() => {
        // コンポーネントのクリーンアップ
        cleanup()
    });

    // データが存在しない場合、タイトルとボタンのみが表示される
    test('given data is not existed when renders app then see only title and text input and button', () => {
        spyFetchGetJson.mockResolvedValue([])

        render(<App/>)

        expect(screen.getByText("My Todo")).toBeInTheDocument()
        expect(screen.getByRole("textbox")).toBeInTheDocument()
        expect(screen.getByRole("button", {name: "追加"})).toBeInTheDocument()
    })

    // データが存在する場合、todo_item が表示される
    test("given data is existed when render then see todo items", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "todo1", isCompleted: false}])

        render(<App/>)

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo")
            expect(screen.getByText("todo1")).toBeInTheDocument()
        })
    })

    // todo_itemが追加される
    test("when write new item and click button then fetch post request", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "Hello World", isCompleted: false}])

        render(<App/>)

        await userEvent.type(screen.getByRole("textbox"), "Hello World")
        await userEvent.click(screen.getByRole("button", {name: "追加"}))

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({content: "Hello World"}),
            })
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.getByRole("textbox")).toHaveValue("")
        })
    })

    // 完了のtodo_itemは一覧に表示されない
    test("when data is existed but completed status when render then cannot see todo item", async () => {
        spyFetchGetJson.mockResolvedValue([
            {id: "1", content: "Hello World", isCompleted: true},
            {id: "2", content: "Jun desu", isCompleted: false}
        ])

        render(<App/>)
        expect(await screen.findByText("Jun desu")).toBeInTheDocument()
        // getByTextだと、表示されない要素はエラーとなるため、要素がないかテストするときは、queryByTextを使う
        expect(screen.queryByText("Hello World")).not.toBeInTheDocument()
    })

    // todo_itemが入力されている場合、Enterキーを押すとtodo_itemが追加される
    test("when write new item press enter then fetch post request", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "Hello World", isCompleted: false}])

        render(<App/>)

        await userEvent.type(screen.getByRole("textbox"), "Hello World")
        await userEvent.keyboard('{Enter}')

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({content: "Hello World"}),
            })
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.getByRole("textbox")).toHaveValue("")
        })
    })

    // todo_item入力内容が空の場合、追加ボタンがdisabled:trueである
    test("The add button should be disabled when the todo item input is empty", async ()=>{
        render(<App/>)

        const todoInput = screen.getByRole('textbox');
        await userEvent.clear(todoInput)

        expect(todoInput).toHaveValue('')
        expect(screen.getByRole("button", {name: "追加"})).toBeDisabled();
    })

    test("完了ボタンを押した場合、todo_itemが完了になる", async () => {
        spyFetchGetJson.mockResolvedValueOnce([
            {id: "1", content: "Hello World", isCompleted: false},
            {id: "2", content: "Hello DIG", isCompleted: false},
        ])

        render(<App/>)

        const todoItem = await screen.findByText("Hello DIG")
        const todoItemDIv = todoItem.closest("div")
        const completeButton = within(todoItemDIv!).getByText("完了")

        spyFetchGetJson.mockResolvedValueOnce([
            {id: "1", content: "Hello World", isCompleted: false},
            {id: "2", content: "Hello DIG", isCompleted: true},

        ])

        await userEvent.click(completeButton)

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo/2", {
                method: "PATCH",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({id: "2", content: "Hello DIG", isCompleted: true}),
            })
        })

        await waitFor(() => {
            expect(screen.queryByText("Hello DIG")).not.toBeInTheDocument();
        });

        expect(screen.getByText("Hello World")).toBeInTheDocument()

    })

    test("削除ボタンを押した場合、todo_item一覧から消える", async () => {
        spyFetchGetJson.mockResolvedValueOnce([
            {id: "1", content: "Hello World", isCompleted: false},
            {id: "2", content: "Hello DIG", isCompleted: false},
        ])

        render(<App/>)

        const todoItem = await screen.findByText("Hello DIG")
        const todoItemDIv = todoItem.closest("div")
        const removeButton = within(todoItemDIv!).getByText("削除")

        spyFetchGetJson.mockResolvedValueOnce([
            {id: "1", content: "Hello World", isCompleted: false}
        ])

        await userEvent.click(removeButton)

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo/2", {
                method: "DELETE",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({id: "2", content: "Hello DIG", isCompleted: false}),
            })
        })

        await waitFor(() => {
            expect(screen.queryByText("Hello DIG")).not.toBeInTheDocument();
        });

        expect(screen.getByText("Hello World")).toBeInTheDocument()

    })

    test("編集ボタンを押した場合、todo_item一覧から消える", async () => {
        spyFetchGetJson.mockResolvedValueOnce([
            { id: "1", content: "Hello World", isCompleted: false },
            { id: "2", content: "Hello DIG", isCompleted: false },
        ]);

        render(<App />);

        const todoItem = await screen.findByText("Hello DIG");
        const todoItemDiv = todoItem.closest("div");
        const editButton = within(todoItemDiv!).getByText("編集");

        await userEvent.click(editButton);

        await waitFor(() => {
            expect(screen.queryByText("Hello DIG")).not.toBeInTheDocument();
        });
    });

    test("編集ボタンを押した場合、todo_inputに表示される", async () => {
        spyFetchGetJson.mockResolvedValueOnce([
            { id: "1", content: "Hello World", isCompleted: false },
            { id: "2", content: "Hello DIG", isCompleted: false },
        ]);

        render(<App />);

        const todoItem = await screen.findByText("Hello DIG");
        const todoItemDiv = todoItem.closest("div");
        const editButton = within(todoItemDiv!).getByText("編集");

        await userEvent.click(editButton);

        await waitFor(() => {
            expect(screen.getByRole("textbox")).toHaveValue("Hello DIG")
        });
    });

    test("編集ボタンを押した場合、編集・キャンセルボタンが表示される", async () => {
        spyFetchGetJson.mockResolvedValueOnce([
            { id: "1", content: "Hello World", isCompleted: false },
            { id: "2", content: "Hello DIG", isCompleted: false },
        ]);

        render(<App />);

        const todoItem = await screen.findByText("Hello DIG");
        const todoItemDiv = todoItem.closest("div");
        const editButton = within(todoItemDiv!).getByText("編集");

        await userEvent.click(editButton);

        await waitFor(() => {
            expect(screen.getByRole("button",{name:"編集"})).toBeInTheDocument()
            expect(screen.getByRole("button",{name:"キャンセル"})).toBeInTheDocument()
        });
    });

    test("キャンセルボタンを押した場合、todo_item一覧に追加される", async () => {
        spyFetchGetJson.mockResolvedValue([
            { id: "1", content: "Hello World", isCompleted: false },
            { id: "2", content: "Hello DIG", isCompleted: false },
        ]);

        render(<App />);

        const todoItem = await screen.findByText("Hello DIG");
        const todoItemDiv = todoItem.closest("div");
        const editButton = within(todoItemDiv!).getByText("編集");

        await userEvent.click(editButton);

        const cancelButton = await screen.findByText("キャンセル")

        await userEvent.click(cancelButton);
    });

    test("編集ボタンを押した場合、todo_itemが更新される", async () => {
        spyFetchGetJson.mockResolvedValueOnce([
            { id: "1", content: "Hello World", isCompleted: false },
            { id: "2", content: "Hello DIG", isCompleted: false },
        ]);

        render(<App />);

        const todoItem = await screen.findByText("Hello DIG");
        const todoItemDiv = todoItem.closest("div");
        const editButton = within(todoItemDiv!).getByText("編集");

        await userEvent.click(editButton);

        await userEvent.type(screen.getByRole("textbox"), "Hello TOYOTA")
        const updateButton = await screen.findByText("変更")

        spyFetchGetJson.mockResolvedValueOnce([
            { id: "1", content: "Hello World", isCompleted: false },
            { id: "2", content: "Hello TOYOTA", isCompleted: false },
        ]);

        await userEvent.click(updateButton);

        await waitFor(() => {
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.queryByText("Hello TOYOTA")).toBeInTheDocument()
        });
    });
})

// query function -> getBy-, findBy-, queryBy-...
// getBy- 1. data O -> return DOM object, data X -> throw exception
// queryBy- 1. data O -> return DOM object, data X -> return null
// findBy - 1. data O -> return Promise(DOM), data X -> throw exception