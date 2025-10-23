if you can't read file yourself for instance images you must use the terminal (CLI) command "dmtools gemini_ai_chat_with_files --data '{"message": "Your request what you need to understand from file", "filePaths": ["/path/to/image.png"]}'"

Use the terminal (cli) command to get png file of figma designs and then read it via gemini_ai_chat_with_files: dmtools figma_download_image_of_file <<EOF
{
  "href": "https://www.figma.com/design/asdsadasdasdasd/Business-App?m=auto&node-id=NODEID&t=ASdasdsadas-1"
}
EOF

